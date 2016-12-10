import java.util.Set;

import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;

public class CampaignData {
		/* campaign attributes as set by server */
		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		public AdxQuery[] campaignQueries;//array of queries relvent for the campaign.
		double ucsCummulativeCost;
		
		/* campaign info as reported */
		CampaignStats stats;
		double budget;
		double[] imprPerDay;
		
		long campaignLenght;
		double segmentSize;
		double reachFactor;
				
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
			ucsCummulativeCost=0;
			int size=(int) (dayEnd-dayStart+1);
			imprPerDay = new double [size];
			
			campaignLenght = dayEnd - dayStart + 1;
			segmentSize  = Population.getSizeSegment(targetSegment);
			reachFactor  = reachImps / (segmentSize*campaignLenght);
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
			
			ucsCummulativeCost=0;
			int size=(int) (dayEnd-dayStart+1);
			imprPerDay = new double [size];
			
			campaignLenght = dayEnd - dayStart + 1;
			segmentSize  = Population.getSizeSegment(targetSegment);
			reachFactor  = reachImps / (segmentSize*campaignLenght);
		}
		
		public void setBudget(double d) {
			budget = d;
		}
		public double getImpsOnDay(int day){
			if(day < dayStart || day > dayEnd)
				return 0.0;
			int index=(int) (day-dayStart);
			return imprPerDay[index];
		}
		public  void updateImpsOnDay(int day, double cumulativeImps){
			if(day < dayStart || day > dayEnd)
				System.out.println("ERROR ! Set impression on a non existing day");
			double lastDayImps= getImpsOnDay(day-1);
			int index=(int) (day-dayStart);
			imprPerDay[index]=cumulativeImps-lastDayImps;
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