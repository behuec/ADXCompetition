

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BankStatus;

/**
 * 
 * @author Mariano Schain
 * Test plug-in
 * 
 */
public class ArgentAdNetwork extends Agent {

	private final Logger log = Logger
			.getLogger(ArgentAdNetwork.class.getName());

	/*
	 * Basic simulation information. An agent should receive the {@link
	 * StartInfo} at the beginning of the game or during recovery.
	 */
	@SuppressWarnings("unused")
	private StartInfo startInfo;

	/**
	 * Messages received:
	 * 
	 * We keep all the {@link CampaignReport campaign reports} delivered to the
	 * agent. We also keep the initialization messages {@link PublisherCatalog}
	 * and {@link InitialCampaignMessage} and the most recent messages and
	 * reports {@link CampaignOpportunityMessage}, {@link CampaignReport}, and
	 * {@link AdNetworkDailyNotification}.
	 */
	private final Queue<CampaignReport> campaignReports;
	private PublisherCatalog publisherCatalog;
	
	private HashMap<String, PublisherData> publishers;
	
	private InitialCampaignMessage initialCampaignMessage;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	/*
	 * The addresses of server entities to which the agent should send the daily
	 * bids data
	 */
	private String demandAgentAddress;
	private String adxAgentAddress;

	/*
	 * we maintain a list of queries - each characterized by the web site (the
	 * publisher), the device type, the ad type, and the user market segment
	 */
	private AdxQuery[] queries;

	/**
	 * Information regarding the latest campaign opportunity announced
	 */
	private CampaignData pendingCampaign;

	/**
	 * We maintain a collection (mapped by the campaign id) of the campaigns won
	 * by our agent.
	 */
	private Map<Integer, CampaignData> myCampaigns;

	/*
	 * the bidBundle to be sent daily to the AdX
	 */
	private AdxBidBundle bidBundle;

	/*
	 * The current bid level for the user classification service
	 */
	private double ucsBid;

	/*
	 * The targeted service level for the user classification service
	 */
	private double ucsTargetLevel=0.8;
	private double decreasingUcsBidFactor=0.95; //TODO: find best factors!
	private double increasingUcsBidFactor=1.15;
	
	//Keep track of ucs bid and level obtained over the days
	private double[][] ucsHistory= new double [Data.TGamedays+1][2];
	/*
	 * The current quality rating
	 */
	private double qualityRating;

	/*
	 * current day of simulation
	 */
	private int day;
	private String[] publisherNames;
	private CampaignData currCampaign;
	
	public ArgentAdNetwork() {
		campaignReports = new LinkedList<CampaignReport>();
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();

			// log.fine(message.getContent().getClass().toString());

			if (content instanceof InitialCampaignMessage) {
				handleInitialCampaignMessage((InitialCampaignMessage) content);
			} else if (content instanceof CampaignOpportunityMessage) {
				handleICampaignOpportunityMessage((CampaignOpportunityMessage) content);
			} else if (content instanceof CampaignReport) {
				handleCampaignReport((CampaignReport) content);
			} else if (content instanceof AdNetworkDailyNotification) {
				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
			} else if (content instanceof AdxPublisherReport) {
				handleAdxPublisherReport((AdxPublisherReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);
			} else if (content instanceof PublisherCatalog) {
				handlePublisherCatalog((PublisherCatalog) content);
			} else if (content instanceof AdNetworkReport) {
				handleAdNetworkReport((AdNetworkReport) content);
			} else if (content instanceof StartInfo) {
				handleStartInfo((StartInfo) content);
			} else if (content instanceof BankStatus) {
				handleBankStatus((BankStatus) content);
			} else if(content instanceof CampaignAuctionReport) {
				handleCampaignAuctionReport((CampaignAuctionReport) content);
			} else if (content instanceof ReservePriceInfo) {
				// ((ReservePriceInfo)content).getReservePriceType();
			} else {
				System.out.println("UNKNOWN Message Received: " + content);
			}

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE,
					"Exception thrown while trying to parse message." + e);
			return;
		}
	}

	private void handleCampaignAuctionReport(CampaignAuctionReport content) {
		// ingoring - this message is obsolete
	}

	private void handleBankStatus(BankStatus content) {
		System.out.println("Day " + day + " :" + content.toString());
	}

	/**
	 * Processes the start information.
	 * 
	 * @param startInfo
	 *            the start information.
	 */
	protected void handleStartInfo(StartInfo startInfo) {
		this.startInfo = startInfo;
	}

	/**
	 * Process the reported set of publishers
	 * 
	 * @param publisherCatalog
	 */
	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
		getPublishersNames();
		this.publishers=new HashMap<>();
		
		for( PublisherCatalogEntry  publisherKey: publisherCatalog.getPublishers()){
			String name = publisherKey.getPublisherName();
			System.out.println("Add "+name+" to publishers");
			PublisherData publisher=new PublisherData(name);
			/*if(publisher==null){
				System.out.println("publisher null!?");
			}else{
				System.out.println("publisher popularity = "+publisher.getPopularity());
			}
			System.out.println("publisher created : "+publisher);*/
			publishers.put(name, publisher );
			
		}
		System.out.println("Publishers initialised : "+publishers);
	}
	
	/**
	 * On day 0, a campaign (the "initial campaign") is allocated to each
	 * competing agent. The campaign starts on day 1. The address of the
	 * server's AdxAgent (to which bid bundles are sent) and DemandAgent (to
	 * which bids regarding campaign opportunities may be sent in subsequent
	 * days) are also reported in the initial campaign message
	 */
	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
		System.out.println(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);
		campaignData.setBudget(initialCampaignMessage.getBudgetMillis()/1000.0);
		currCampaign = campaignData;
		genCampaignQueries(currCampaign);

		/*
		 * The initial campaign is already allocated to our agent so we add it
		 * to our allocated-campaigns list.
		 */
		System.out.println("Day " + day + ": Allocated campaign - " + campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}
	private int campaignsRunningNextDay(){
		int runningNextDay=0;
		for(Entry<Integer, CampaignData> camp :myCampaigns.entrySet()){
			CampaignData campData= camp.getValue();
			if(campData.dayStart<=day+1 && campData.dayEnd>=day+1)
				runningNextDay++;
		}
		return runningNextDay;
	}
	/**
	 * On day n ( > 0) a campaign opportunity is announced to the competing
	 * agents. The campaign starts on day n + 2 or later and the agents may send
	 * (on day n) related bids (attempting to win the campaign). The allocation
	 * (the winner) is announced to the competing agents during day n + 1.
	 */
	private void handleICampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		day = com.getDay();

		pendingCampaign = new CampaignData(com);
		System.out.println("Day " + day + ": Campaign opportunity - " + pendingCampaign);
		Random random = new Random();
		
		/*
		 * The campaign requires com.getReachImps() impressions. The competing
		 * Ad Networks bid for the total campaign Budget (that is, the ad
		 * network that offers the lowest budget gets the campaign allocated).
		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
		 * therefore the total number of impressions may be treated as a reserve
		 * (upper bound) price for the auction.
		 * 
		 * Old Code:
		 * long cmpimps = com.getReachImps();
		 * long cmpBidMillis = random.nextInt((int)cmpimps);
		 */
		
		/* 
		 * Bid parameters for Campaign opportunity:
		 * Rmin = 0.0001 
		 * Rmax = 0.001
		 * Quality € [ 0 , 1.385 ]
		 * EffectiveBid = Quality / Bid
		 * Constraints: 	Bid * Quality > Creach * Rmin
		 * 					Bid / Quality < Creach * Rmax
		 * so:	
		 * 		( Creach * Rmin ) / Quality < Bid < Quality * Creach * Rmax  
		 */
		
		long Creach = com.getReachImps(); 
		double upperBound = qualityRating * Creach * Data.RCampaignMax ;
		double lowerBound = ( Creach * Data.RCampaignMin ) / qualityRating;
		//long cmpBidMillis = (long)(random.nextDouble()*(upperBound - lowerBound) + lowerBound);
		long cmpimps = com.getReachImps();
		long cmpBidMillis = random.nextInt((int)cmpimps);
		System.out.println("Day " + day + ": Campaign total budget bid (millis): " + cmpBidMillis);

		/*
		 * Adjust ucs bid s.t. target level is achieved. Note: The bid for the
		 * user classification service is piggybacked
		 */

		if (adNetworkDailyNotification != null) {
			double ucsLevel = adNetworkDailyNotification.getServiceLevel();
			ucsHistory[day][1]=ucsLevel;

			if(campaignsRunningNextDay()==0)
				//We don't care about UCS level when we don't have any campaign running next day.
				ucsBid=0;
			else{
				int previousNotNullBidDay=day-1;
				//search the last time we didn't bid 0:
				while(previousNotNullBidDay > 0){
					if(ucsHistory[previousNotNullBidDay][0]!=0)
						break;
					previousNotNullBidDay--;
				}
				if(previousNotNullBidDay!=0){
					
					ucsBid=ucsHistory[previousNotNullBidDay][0];
					
					if(ucsHistory[previousNotNullBidDay+1][1] > ucsTargetLevel){
						ucsBid=ucsBid*decreasingUcsBidFactor;
					}else {
						if(ucsHistory[previousNotNullBidDay+1][1] > ucsTargetLevel)
							ucsBid=ucsBid*increasingUcsBidFactor;
											
					}
				}else{// The first time we bid randomly:
					
					ucsBid = 0.1 + random.nextDouble()/10.0;
				}
			}
			ucsHistory[day][0]=ucsBid;
			System.out.println("Day " + day + ": ucs level reported: " + ucsLevel);
		} else {
			System.out.println("Day " + day + ": Initial ucs bid is " + ucsBid);
		}

		/* Note: Campaign bid is in millis */
		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.id, cmpBidMillis);
		sendMessage(demandAgentAddress, bids);
	}

	/**
	 * On day n ( > 0), the result of the UserClassificationService and Campaign
	 * auctions (for which the competing agents sent bids during day n -1) are
	 * reported. The reported Campaign starts in day n+1 or later and the user
	 * classification service level is applicable starting from day n+1.
	 */
	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification notificationMessage) {

		adNetworkDailyNotification = notificationMessage;

		System.out.println("Day " + day + ": Daily notification for campaign "
				+ adNetworkDailyNotification.getCampaignId());

		String campaignAllocatedTo = " allocated to "
				+ notificationMessage.getWinner();

		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& (notificationMessage.getCostMillis() != 0)) { //cost!=0 only if we won it ?

			/* add campaign to list of won campaigns */
			System.out.println("Campaign won, notificationMessage.getCostMillis()="+notificationMessage.getCostMillis());
			pendingCampaign.setBudget(notificationMessage.getCostMillis()/1000.0);
			currCampaign = pendingCampaign;
			genCampaignQueries(currCampaign);
			myCampaigns.put(pendingCampaign.id, pendingCampaign);

			campaignAllocatedTo = " WON at cost (Millis)"
					+ notificationMessage.getCostMillis();
		}
		
		// save the new qR to calculate effective bid 
		qualityRating = notificationMessage.getQualityScore();
		
		System.out.println("Day " + day + ": " + campaignAllocatedTo
				+ ". UCS Level set to " + notificationMessage.getServiceLevel()
				+ " at price " + notificationMessage.getPrice()
				+ " Quality Score is: " + qualityRating);
	}

	/**
	 * The SimulationStatus message received on day n indicates that the
	 * calculation time is up and the agent is requested to send its bid bundle
	 * to the AdX.
	 */
	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		System.out.println("Day " + day + " : Simulation Status Received");
		sendBidAndAds();
		System.out.println("Day " + day + " ended. Starting next day");
		++day;
	}

	/**
	 * 
	 */
	protected void sendBidAndAds() {
		bidBundle = new AdxBidBundle();

		int dayBiddingFor = day + 1;

		Random random = new Random();

		/* A random bid, fixed for all queries of the campaign */
		/*
		 * Note: bidding per 1000 imps (CPM) - no more than average budget
		 * revenue per imp
		 */
		double bid; 
		
		/*
		 * add bid entries w.r.t. each active campaign with remaining contracted
		 * impressions.
		 * 
		 * for now, a single entry per active campaign is added for queries of
		 * matching target segment.
		 */
		for(CampaignData  camp: myCampaigns.values()){
			System.out.println("Traitement Campagne "+camp.id+ "Commençant le "+camp.dayStart+" et finissant le "+camp.dayEnd+"dont l'impTogo="+camp.impsTogo());
			if ((dayBiddingFor >= camp.dayStart)
					&& (dayBiddingFor <= camp.dayEnd)
					&& (camp.impsTogo() > 0)) {
	//TODO: suppr impsToGo ? because more impressions is best? or put impsToGo*1.1?
				int entCount = 0;
				for (AdxQuery query : camp.campaignQueries) {
					//System.out.println("query = "+query);
					//if (camp.impsTogo() - entCount > 0) {
						/*
						 * among matching entries with the same campaign id, the AdX
						 * randomly chooses an entry according to the designated
						 * weight. by setting a constant weight 1, we create a
						 * uniform probability over active campaigns
						 * (irrelevant because we are bidding only on one campaign)
						 */
						/*if (query.getDevice() == Device.pc) {
							if (query.getAdType() == AdType.text) {
								entCount++;
							} else {
								entCount += camp.videoCoef;
							}
						} else {
							if (query.getAdType() == AdType.text) {
								entCount+=camp.mobileCoef;
							} else {
								entCount += camp.videoCoef + camp.mobileCoef;
							}
	
						}*/
						double maxBid = camp.budget-camp.stats.getCost()/camp.impsTogo() ;//bid/1000 if we bid in single impression.
						AdxPublisherReportEntry publisher = publishers.get(query.getPublisher());
						double minBid = publisher.getReservePriceBaseline();
						//System.out.println("min (publisher reserve price)= "+minBid+" max (camp budget)= "+maxBid);
						if(maxBid > minBid){ //We only bid if it worths it ?
							//TODO : Modify bid to get a more accurate value given the publisher.get Popularity, AdxType etc.
							bid = random.nextDouble()*(maxBid-minBid)+minBid;

							//System.out.println("we bid "+bid);
							//TODO: changer rbid en fct de si les devices, publishers et adtype sont bns pr le segment.
							bidBundle.addQuery(query, bid, new Ad(null), camp.id, 1);
						}
					//}
				}
				double impressionLimit = camp.impsTogo();
				//changed:
				double budgetLimit = camp.budget-camp.stats.getCost();
				bidBundle.setCampaignDailyLimit(camp.id,
						(int) impressionLimit, budgetLimit);
	
				System.out.println("Day " + day + ": Updated " + entCount
						+ " Bid Bundle entries for Campaign id " + camp.id);
			}
		}
		if (bidBundle != null) {
			System.out.println("Day " + day + ": Sending BidBundle");
			sendMessage(adxAgentAddress, bidBundle);
		}
	}

	/**
	 * Campaigns performance w.r.t. each allocated campaign
	 */
	private void handleCampaignReport(CampaignReport campaignReport) {

		campaignReports.add(campaignReport);

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();
			myCampaigns.get(cmpId).setStats(cstats);

			System.out.println("Day " + day + ": Updating campaign " + cmpId + " stats: "
					+ cstats.getTargetedImps() + " tgtImps "
					+ cstats.getOtherImps() + " nonTgtImps. Cost of imps is "
					+ cstats.getCost());
		}
	}

	/**
	 * Users and Publishers statistics: popularity and ad type orientation
	 */
	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		
		System.out.println("Publishers Report: ");
		for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
			AdxPublisherReportEntry entry = adxPublisherReport
					.getEntry(publisherKey);
			System.out.println(entry.toString());
			
		}
		
		for( PublisherCatalogEntry  publisherKey: publisherCatalog.getPublishers()){
			AdxPublisherReportEntry entry = adxPublisherReport.getEntry(publisherKey);
			
			PublisherData publisher = publishers.get(entry.getPublisherName());
			
			publisher.setPopularity(entry.getPopularity());			
			publisher.setAdTypeOrientation(entry.getAdTypeOrientation());
			publisher.setReservePriceBaseline(entry.getReservePriceBaseline());
		}
	}

	/**
	 * 
	 * @param AdNetworkReport
	 */
	private void handleAdNetworkReport(AdNetworkReport adnetReport) {

		System.out.println("Day " + day + " : AdNetworkReport");
		/*
		 * for (AdNetworkKey adnetKey : adnetReport.keys()) {
		 * 
		 * double rnd = Math.random(); if (rnd > 0.95) { AdNetworkReportEntry
		 * entry = adnetReport .getAdNetworkReportEntry(adnetKey);
		 * System.out.println(adnetKey + " " + entry); } }
		 */
	}

	@Override
	protected void simulationSetup() {
		Random random = new Random();

		day = 0;
		bidBundle = new AdxBidBundle();

		/* initial bid between 0.1 and 0.2 */
		ucsBid = 0.1 + random.nextDouble()/10.0;

		myCampaigns = new HashMap<Integer, CampaignData>();
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		bidBundle = null;
	}

	/**
	 * A user visit to a publisher's web-site results in an impression
	 * opportunity (a query) that is characterized by the the publisher, the
	 * market segment the user may belongs to, the device used (mobile or
	 * desktop) and the ad type (text or video).
	 * 
	 * An array of all possible queries is generated here, based on the
	 * publisher names reported at game initialization in the publishers catalog
	 * message
	 */
	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> querySet = new HashSet<AdxQuery>();

			/*
			 * for each web site (publisher) we generate all possible variations
			 * of device type, ad type, and user market segment
			 */
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				String publishersName = publisherCatalogEntry
						.getPublisherName();
				for (MarketSegment userSegment : MarketSegment.values()) {
					Set<MarketSegment> singleMarketSegment = new HashSet<MarketSegment>();
					singleMarketSegment.add(userSegment);

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.video));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.video));

				}

				/**
				 * An empty segments set is used to indicate the "UNKNOWN"
				 * segment such queries are matched when the UCS fails to
				 * recover the user's segments.
				 */
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.text));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.text));
			}
			queries = new AdxQuery[querySet.size()];
			querySet.toArray(queries);
		}
	}
	
	/*genarates an array of the publishers names
	 * */
	private void getPublishersNames() {
		if (null == publisherNames && publisherCatalog != null) {
			ArrayList<String> names = new ArrayList<String>();
			for (PublisherCatalogEntry pce : publisherCatalog) {
				names.add(pce.getPublisherName());
			}

			publisherNames = new String[names.size()];
			names.toArray(publisherNames);
		}
	}
	/*
	 * genarates the campaign queries relevant for the specific campaign, and assign them as the campaigns campaignQueries field 
	 */
	private void genCampaignQueries(CampaignData campaignData) {
		Set<AdxQuery> campaignQueriesSet = new HashSet<AdxQuery>();
		for (String PublisherName : publisherNames) {
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.video));
		}

		campaignData.campaignQueries = new AdxQuery[campaignQueriesSet.size()];
		campaignQueriesSet.toArray(campaignData.campaignQueries);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+Arrays.toString(campaignData.campaignQueries)+"!!!!!!!!!!!!!!!!");
		

	}

	private class CampaignData {
		/* campaign attributes as set by server */
		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		private AdxQuery[] campaignQueries;//array of queries relvent for the campaign.

		/* campaign info as reported */
		CampaignStats stats;
		double budget;

		public CampaignData(InitialCampaignMessage icm) {
			reachImps = icm.getReachImps();
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			targetSegment = icm.getTargetSegment();
			videoCoef = icm.getVideoCoef();
			mobileCoef = icm.getMobileCoef();
			id = icm.getId();

			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		public void setBudget(double d) {
			budget = d;
		}

		public CampaignData(CampaignOpportunityMessage com) {
			dayStart = com.getDayStart();
			dayEnd = com.getDayEnd();
			id = com.getId();
			reachImps = com.getReachImps();
			targetSegment = com.getTargetSegment();
			mobileCoef = com.getMobileCoef();
			videoCoef = com.getVideoCoef();
			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		@Override
		public String toString() {
			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
					+ dayEnd + " " + targetSegment + ", reach: " + reachImps
					+ " coefs: (v=" + videoCoef + ", m=" + mobileCoef + ")";
		}

		int impsTogo() {
			return (int) Math.max(0, reachImps - stats.getTargetedImps());
		}

		void setStats(CampaignStats s) {
			stats.setValues(s);
		}

		public AdxQuery[] getCampaignQueries() {
			return campaignQueries;
		}

		public void setCampaignQueries(AdxQuery[] campaignQueries) {
			this.campaignQueries = campaignQueries;
		}

	}

}
