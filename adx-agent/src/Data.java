
public class Data {

		//User continuation Probability and number max:
		public final static float PContinue = 0.3f;
		public final static int NContinueMax = 6;
		
		//Probability of random allocation of a campaign:
		public final static float PRandomCampaign = 0.36f;
		
		//Publishers' Reserve price parameters:
		public final static float RReserveInit = 0.005f;
		public final static float RVariance = 0.02f;
		public final static float RLearnRate = 0.2f;
		
		//Duration (in days) of campaigns:
		public final static int CCampaignL1 = 3;
		public final static int CCampaignL2 = 5;
		public final static int CCampaignL3 = 10;
		

		//Low - Medium - High reach factors :
		public final static float CCampaignLowReach = 0.2f;
		public final static float CCampaignMediumReach = 0.5f;
		public final static float CCampaignHighReach = 0.8f;
		
		//Max & Min Campaign Cost per Impression:
		public final static float RCampaignMax = 0.001f;
		public final static float RCampaignMin = 0.0001f;
		
		//Quality rating learning rate
		public final static float LRating = 0.6f;
		
		//Game time & "Simulation day" time
		public final static int TGamedays = 60;
		public final static int TDaysseconds = 10;
		
		//UCS Revelation Probability & Initial Accuracy:
		public final static float PUserRevelation = 0.9f;
		public final static float ZUCSAccuracy = 0.9f;
		
		//Users possible ages
		public enum Age {
			age18_24, age25_34, age45_54 , age55_64, age65_plus
		}
		
		public enum Income {
			income0_30, income30_60, income60_100, income100_plus
		}
		
		// Effective Reach Ration used to compute
		public double ERR( double eta){
			return 0.4895 * (Math.atan(4.08577*eta-3.08577) + 1.2574); 
		}
		
		//TODO: put all table 3 (info on publishers)
		
		
}
