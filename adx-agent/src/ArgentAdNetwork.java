

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
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
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
	private double ucsTargetLevel=0.81;
	private double decreasingUcsBidFactor=0.95; //TODO: find best factors!
	private double increasingUcsBidFactor=1.1;
	
	//number of impressions get the previous day, over all contracts.
	double totImpGetYesterday;
	
	//Keep track of ucs bid, level & price obtained over the days.
	// example : 	ucsHistory[0][0] gives the ucs bid created day 0 but for adx auctions on day 2
	//				ucsHistory[0][1] gives the ucs level officialy obtained on day 0 but for adx auctions on day 1
	//				ucsHistory[0][2] gives the ucs price officialy obtained on day 0 but for adx auctions on day 1
	//				ucsHistory[0][2] gives the number of campaigns running on day 0.
	
	private double[][] ucsHistory= new double [Data.TGamedays+1][4];
	/*
	 * The current quality rating
	 */
	private double qualityRating;
	private Population population;
	/*
	 * current day of simulation
	 */
	private int day;
	private String[] publisherNames;
	private CampaignData currCampaign;
	
	/*
	 * 
	 */
	private CompetitionTracker internalCompetition = new CompetitionTracker();
	private CompetitionTracker externalCompetition = new CompetitionTracker();
	
	private int unluckyStreak = 0;
	
	public ArgentAdNetwork() {
		campaignReports = new LinkedList<CampaignReport>();
		population=new Population();
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
		ucsHistory[1][1]=Data.ZUCSAccuracy;
		ucsHistory[1][2]=0.0; //ucs furnished at no cost
		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);
		campaignData.setBudget(initialCampaignMessage.getBudgetMillis()/1000.0);
		currCampaign = campaignData;
		genCampaignQueries(currCampaign);
		
		internalCompetition.addCampaign(currCampaign);

		/*
		 * The initial campaign is already allocated to our agent so we add it
		 * to our allocated-campaigns list.
		 */
		System.out.println("Day " + day + ": Allocated campaign - " + campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}
	private int campaignsRunningNextNextDay(){
		int runningNextDay=0;
		for(Entry<Integer, CampaignData> camp : myCampaigns.entrySet()){
			CampaignData campData= camp.getValue();
			if(campData.dayStart<=day+2 && campData.dayEnd>=day+2)
				runningNextDay++;
		}
		return runningNextDay;
	}
	
	/*
	 * Remove expired campaigns from the winning campaigns
	 * TODO: check if the dayEnd is inclusive or not
	 */
	private void removeExpiredCampaings( int currentDay ){
		Map<Integer, CampaignData> updated_camp = new HashMap<Integer, CampaignData>();
		for(Entry<Integer, CampaignData> camp : myCampaigns.entrySet()){
			Integer campId = camp.getKey();
			CampaignData campData= camp.getValue();
			if(campData.dayEnd >= currentDay)
				updated_camp.put(campId, campData);
		}
		myCampaigns = updated_camp;
	}
	
	public int campaignRunningYesterday(long day){
		int numberOfCampaignRunning=0;
		for (Entry<Integer, CampaignData> campaign : myCampaigns.entrySet()){
			CampaignData campData = campaign.getValue();
			if(campData.dayStart <= day && campData.dayEnd >=day){
				numberOfCampaignRunning++;
			}
		}
		return numberOfCampaignRunning;
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
		//we compute the number of campaign running yesterday before removing old ones
		if(day!=0)
			ucsHistory[day-1][3]=campaignRunningYesterday(day-1);
		removeExpiredCampaings(day);
		
		pendingCampaign = new CampaignData(com);
		System.out.println("Day " + day + ": Campaign opportunity - " + pendingCampaign);
		
		/*
		Boolean reachable = internalCompetition.isReachable(pendingCampaign); 
		Boolean reachable2 = externalCompetition.isReachable(pendingCampaign);
		System.out.println("-------CAMPAIGN RECHABLE ?---------");
		System.out.println("-------Internally : "+reachable+"---------");
		System.out.println("-------Externally: "+reachable2+"---------");
		*/
		
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
		
		System.out.println("Computing:");
		long Creach = com.getReachImps() * 1000;
		System.out.println("Creach: "+Creach);
		double upperBound = qualityRating * Creach * Data.RCampaignMax ;
		double lowerBound = ( Creach * Data.RCampaignMin ) / qualityRating;
		System.out.println("lowerBound: "+lowerBound);
		//long cmpBidMillis = (long)(random.nextDouble()*(upperBound - lowerBound) + lowerBound);
		long cmpBidMillis = (long)lowerBound + 1;
		
		double intComp = 0.0;
		double extComp = 0.0;
		double intCompDay = 0.0;
		double extCompDay = 0.0;
		boolean hotspot = false;
		long campaignLenght = pendingCampaign.campaignLength;
		double segmentSize  = pendingCampaign.segmentSize;
		double reachFactor  = pendingCampaign.reachFactor;
		for(long i = pendingCampaign.dayStart; i <= pendingCampaign.dayEnd; i++){
			intCompDay = internalCompetition.getCompetition(i, pendingCampaign.targetSegment);
			extCompDay = externalCompetition.getCompetition(i, pendingCampaign.targetSegment);
			if(intCompDay > 1.0)
				hotspot = true;
			intComp += intCompDay;
			extComp += extCompDay;
		}
 
		double totComp = ( intComp + extComp + reachFactor ) / pendingCampaign.campaignLength;
		
		if(totComp > 1.0){
			// There is too much competition in the long run, don't bid
			cmpBidMillis = (long) upperBound - 1;
		} 
		else 
		{
			// Check reachability for each day
			if(hotspot){
				// Is going to hinder our campaigns, let's try to balance that with a huge budget;
				cmpBidMillis =  (long) upperBound - 1;
			} else {
				double coefficient = 0.1 * random.nextDouble();
				cmpBidMillis *= (1.0 + coefficient);
			}
		}
		
		if(unluckyStreak >= 10){
			cmpBidMillis = (long)lowerBound + 1;
		}
		
		System.out.println("Day " + day + ": Campaign total budget bid (millis): " + cmpBidMillis);

		/*
		 * Adjust ucs bid s.t. target level is achieved. Note: The bid for the
		 * user classification service is piggybacked
		 */

		if(campaignsRunningNextNextDay()==0){
			//We don't care about UCS level when we don't have any campaign running next day.
			ucsBid=0;
			System.out.println("No campaign => UCS bid=0");
			
		}else{
			if (adNetworkDailyNotification != null) {				
	
				int previousNotNullBidDay=day-1;
				//search the last time we didn't bid 0:
				while(previousNotNullBidDay>=0 && ucsHistory[previousNotNullBidDay][0]==0){
					previousNotNullBidDay--;
				}

				ucsBid=ucsHistory[previousNotNullBidDay][0];
				System.out.println("[UCS] Previous not null UCS bid ="+ucsBid+" on day"+(previousNotNullBidDay));
				
				if(ucsHistory[previousNotNullBidDay+1][1] > ucsTargetLevel){
					System.out.println("[UCS] UCS bid level obtained on this day was ="+ucsHistory[previousNotNullBidDay+1][1]+" we decrease the bid" );
					ucsBid=ucsBid*decreasingUcsBidFactor;
				}else {
					if(ucsHistory[previousNotNullBidDay+1][1] < ucsTargetLevel){
						System.out.println("[UCS] UCS bid level obtained on this day was ="+ucsHistory[previousNotNullBidDay+1][1]+" we increase the bid" );

						ucsBid=ucsBid*increasingUcsBidFactor;
					}else{			
						//System.out.println("UCS bid level obtained on this day was = ucsTargetLevel, we keep it" );
						ucsBid= ucsHistory[previousNotNullBidDay][0] ;
					}
				}
			}else {
				System.out.println("[Warning] daily notif was empty (ucs bid set randomly)");
				if(ucsBid==0){
					ucsBid = 0.1 + random.nextDouble()/5.0;
				}
			}
		}
		ucsHistory[day][0]=ucsBid;
		System.out.println("[UCS] Day " + day + ": UCS bid: " + ucsBid);
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
			internalCompetition.addCampaign(pendingCampaign);
			
			unluckyStreak = 0;

			campaignAllocatedTo = " WON at cost (Millis)"
					+ notificationMessage.getCostMillis();
		} else {
			unluckyStreak++;
			externalCompetition.addCampaign(pendingCampaign);
		}
		externalCompetition.competitionStats("external",day);
		internalCompetition.competitionStats("internal",day);
		System.out.println("Streak: "+unluckyStreak);
		
		// save the new qR to calculate effective bid 
		qualityRating = notificationMessage.getQualityScore();
		
		System.out.println("Day " + day + ": " + campaignAllocatedTo
				+ ". UCS Level set to " + notificationMessage.getServiceLevel()
				+ " at price " + notificationMessage.getPrice()
				+ " Quality Score is: " + qualityRating);
		ucsHistory[day+1][1]=notificationMessage.getServiceLevel();
		ucsHistory[day+1][2]=notificationMessage.getPrice();
	}
	private void printUcsHistory(){
		System.out.println(" [	UCS History: ");
		//we print only day-5 to day+2 (in order to avoid spamming terminal).
		int start = Math.max(0, day-5);
		int end = Math.min(Data.TGamedays, day+2);
		for(int i=start; i<=end; i++){
			System.out.println(" day "+i+": ucsbid ="+ucsHistory[i][0]+", ucslevel="+ucsHistory[i][1]+" ucsprice="+ucsHistory[i][2]+" nbCampaigns="+ucsHistory[i][3]);
		}
		System.out.println(" ]");
	}
	//update the cumulative ucs cost for each campaign according to the proportion of imps they get the last day.
	private void splitUcsCostsBetweenCampaigns(int day){
		double ucsCost = 0;
		if(day>=0)
			ucsCost = ucsHistory[day][2];
		printUcsHistory();
		if(ucsCost!=0){
			if(totImpGetYesterday!=0){ //we won some imps:
				System.out.println("----UCS price was not nul yesterday and we get imps :" +
						" we split the ucs cost among the running campaigns-----");
				for (Entry<Integer, CampaignData> campaign : myCampaigns.entrySet()){
					CampaignData campData = campaign.getValue();
					if(campData.dayStart <= day && campData.dayEnd >=day){
						System.out.println(" imp on day :"+campData.getImpsOnDay(day)+" total imps :"+totImpGetYesterday+" ucs cost :"+ucsCost);
						double ucspart = (campData.getImpsOnDay(day)/totImpGetYesterday)*ucsCost;
						System.out.println(" ucs part : "+ucspart+" for camp "+campData.id);
						campData.ucsCummulativeCost +=ucspart;
					}
				}
			}
			else{ //we paid for nothing. we split the cost equally between our campaigns
				System.out.println("UCS cost wasn't nul yesterday ("+day+") but we didn't get any imps.");
				int numberOfCampaign = (int) ucsHistory[day-1][3];
				
				for (Entry<Integer, CampaignData> campaign : myCampaigns.entrySet()){
					CampaignData campData = campaign.getValue();
					if(campData.dayStart <= day && campData.dayEnd >=day){
						campData.ucsCummulativeCost +=ucsCost/numberOfCampaign;
					}
				}
			}
		}
		else
			System.out.println("----UCS price was nul yesterday ! (yesterday ="+day+")-----");

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
		
		/*
		/*	update ucs cost until yesterday 
		/*	(we don't know yet how to split the cost of today since we don't have the imps results before tomorrow).
		*/
		splitUcsCostsBetweenCampaigns(day-1);
		
		/* 
		 * Note: bidding per 1000 imps (CPM) - no more than average budget
		 * revenue per imp
		 */
		double bid; 
		int dayBiddingFor = day + 1;
		//Random random = new Random();		
		
		/* add bid entries w.r.t. each active campaign with remaining contracted impressions.*/
		for(CampaignData  camp: myCampaigns.values()){
			if ((dayBiddingFor >= camp.dayStart) && (dayBiddingFor <= camp.dayEnd)	&& (camp.impsTogo() > 0)) {			
				//System.out.println("Traitement Campagne "+camp.id+ "Commençant le "+camp.dayStart+" et finissant le "+camp.dayEnd+"dont l'impTogo="+camp.impsTogo());

				for (AdxQuery query : camp.campaignQueries) {
					/*
					 * among matching entries with the same campaign id, the AdX
					 * randomly chooses an entry according to the designated
					 * weight. by setting a constant weight 1, we create a
					 * uniform probability over active campaigns
					 */
					float impToGoMillis = camp.impsTogo()/1000f;
						//System.out.println("camp budget = "+camp.budget+", adxcosts = "+camp.stats.getCost()+", ucscosts = "+camp.ucsCummulativeCost+" impToGo (millis)= "+impToGoMillis);
						
						double maxBid = (camp.budget-camp.stats.getCost()-camp.ucsCummulativeCost)/impToGoMillis ;//bid/1000 if we bid in single impression.
						double entCount=0.0;
						if (query.getDevice() == Device.pc) {
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
						}
						//System.out.println("This unique impression can bring "+entCount+ " impressions");
						//an unique impression represent enCount effective impression, 
						//but maybe we need less impressions than what we could get.
						maxBid=maxBid * Math.min(entCount, impToGoMillis);
						AdxPublisherReportEntry publisher = publishers.get(query.getPublisher());
						double minBid = publisher.getReservePriceBaseline();
						
						//System.out.println("min (publisher reserve price)= "+minBid+" max ((camp budget-costadx -cost ucs)/impTogo)= "+maxBid);
						if(maxBid >= minBid){ //We only bid if it worths it 
							//TODO : Modify bid to get a more accurate value given the publisher.get Popularity, AdxType etc.
							//bid = random.nextDouble()*(maxBid-minBid)+minBid;
							bid =maxBid;
							//System.out.println("we bid "+bid);
							if(camp.dayEnd==dayBiddingFor){ //urgent
								bidBundle.addQuery(query, bid, new Ad(null), camp.id, 2);
							}
							bidBundle.addQuery(query, bid, new Ad(null), camp.id, 1);

							//System.out.println("bidADX = "+bid);
						}
				}
				double impressionLimit = camp.impsTogo();
				//we want to satisfy the campaign imps to go to keep a good quality even if the budget is already gone
				double budgetLimit = (camp.budget-camp.stats.getCost())*1.05;
				bidBundle.setCampaignDailyLimit(camp.id,
						(int) impressionLimit, budgetLimit);
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
		totImpGetYesterday = 0;
		campaignReports.add(campaignReport);

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();	
			
			double new_impressions = cstats.getTargetedImps()+cstats.getOtherImps();
			CampaignData temp_camp = myCampaigns.get(cmpId);
			internalCompetition.updateCompetition(temp_camp.targetSegment, day, (int)temp_camp.dayEnd, new_impressions);
			temp_camp.setStats(cstats);
			temp_camp.updateImpsOnDay(day-1, temp_camp.getImpsOnDay(day-1));
			totImpGetYesterday += temp_camp.getImpsOnDay(day-1);
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

		/*System.out.println("Day " + day + " : AdNetworkReport");
			for (AdNetworkKey adnetKey : adnetReport.keys()) { 
				AdNetworkReportEntry entry = adnetReport .getAdNetworkReportEntry(adnetKey);
				System.out.println(entry);  
			}*/
		 
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
}
