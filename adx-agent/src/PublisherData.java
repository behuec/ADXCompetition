import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;


public class PublisherData extends AdxPublisherReportEntry{
	
	private Map<AdType, Integer>  adOrientation; //video or text
	private Map<Data.Age, Double> ageOrientation; //Initial Stats
	private Map<Data.Income, Integer> incomeOrientation; //Initial Stats
	private double mobileOrientation;
	
	
	public PublisherData(String name) {
		
	
		setReservePriceBaseline(Data.RReserveInit);
		
		switch (name) {
	        case "yahoo":  		initYahoo(); 	break;
	        case "cnn":  		initCnn(); 		break;
	        case "nyt":  		initNytimes(); 	break;
	        case "hfn":  	initHfngtn();	break;
	        case "msn":  		initMsn(); 		break;
	        case "fox":  		initFox(); 		break;
	        case "amazon":  	initAmazon(); 	break;
	        case "ebay":  		initEbay(); 	break;
	        case "wallmart":   	initWalmart(); 	break;
	        case "target": 		initTarget(); 	break;
	        case "bestbuy": 	initBestBuy(); 	break;
	        case "sears": 		initSears(); 	break;
	        case "webmd": 		initWebmd(); 	break;
	        case "ehow":  		initEhow(); 	break;
	        case "ask": 		initAsk(); 		break;
	        case "tripadvisor": initTripadvisor(); break;
	        case "cnet": 		initCnet(); 	break;
			case "weather": 	initWeather(); 	break;
	        default: 
	        	System.out.println("Publisher name not recognized! Initialisation impossible : "+name);
		}
	}
	public double getMobileOrientation() {
		return mobileOrientation;
	}
	public void setMobileOrientation(double mobileOrientation) {
		this.mobileOrientation = mobileOrientation;
	}
	public void initPublisher(int popularity, int mobileOrientation, double[] agesPop, int[] incomePop){
		this.setPopularity(popularity);
		this.setMobileOrientation(mobileOrientation);
		
		Map<Data.Age, Double> ageOrientation= new HashMap<Data.Age, Double>(); 
		ageOrientation.put(Data.Age.Age_18_24, agesPop[0]);
		ageOrientation.put(Data.Age.Age_25_34, agesPop[1]);
		ageOrientation.put(Data.Age.Age_35_44, agesPop[2]);
		ageOrientation.put(Data.Age.Age_45_54, agesPop[3]);
		ageOrientation.put(Data.Age.Age_55_64, agesPop[4]);
		//ageOrientation.put(Data.Age.age65_plus, ...);
		this.ageOrientation=ageOrientation;
		
		Map<Data.Income, Integer> incomeOrientation= new HashMap<Data.Income, Integer>(); 
		incomeOrientation.put(Data.Income.income0_30, incomePop[0]);
		incomeOrientation.put(Data.Income.income30_60, incomePop[1]);
		incomeOrientation.put(Data.Income.income60_100, incomePop[2]);
		//incomeOrientation.put(Data.Income.income100_plus, ...);
		
		this.incomeOrientation=incomeOrientation;
		//adOrientation??
	}
	public void initYahoo() {
		double[] agesPop={12.2,17.1,16.7,18.4,16.4};
		int[] incomePop={53, 27, 13};
		initPublisher(16, 26, agesPop, incomePop);		
	}
	private void initCnn() {
		double[] agesPop={10.2,16.1,16.7,19.4,17.4};
		int[] incomePop={48, 27, 16};
		initPublisher(2, 24, agesPop, incomePop);	//warning : popularity rounded!!	
	}
	private void initNytimes() {
		double[] agesPop={9.2,15.1,16.7,19.4,17.4};
		int[] incomePop={47, 26, 17};
		initPublisher(3, 23, agesPop, incomePop);	//warning : popularity rounded!!			
	}
	private void initHfngtn() {
		double[] agesPop={10.2,16.1,16.7,19.4,17.4};
		int[] incomePop={47, 27, 17};
		initPublisher(8, 22, agesPop, incomePop);	//warning : popularity rounded!!	
	}
	private void initMsn() {
		double[] agesPop={10.2,16.1,16.7,19.4,17.4};
		int[] incomePop={49, 27, 16};
		initPublisher(18, 25, agesPop, incomePop);	//warning : popularity rounded!!
		
	}
	private void initFox() {
		double[] agesPop={9.2,15.1,16.7,19.4,18.4};
		int[] incomePop={46, 26, 18};
		initPublisher(3, 24, agesPop, incomePop);	//warning : popularity rounded!!
		
	}
	private void initAmazon() {
		double[] agesPop={9.2,15.1,16.7,19.4,18.4};
		int[] incomePop={50, 27, 15};
		initPublisher(13, 21, agesPop, incomePop);	//warning : popularity rounded!!
	}
	
	private void initEbay() {
		double[] agesPop={9.2,16.1,15.7,19.4,17.4};
		int[] incomePop={50, 27, 15};
		initPublisher(9, 22, agesPop, incomePop);	//warning : popularity rounded!!
		
	}
	private void initWalmart() {
		double[] agesPop={7.2,15.1,16.7,20.4,18.4};
		int[] incomePop={47, 28, 19};
		initPublisher(4, 18, agesPop, incomePop);	//warning : popularity rounded!!
		
	}
	private void initTarget() {
		double[] agesPop={9.2,17.1,17.7,18.4,17.4};
		int[] incomePop={45, 27, 19};
		initPublisher(2, 19, agesPop, incomePop);	//warning : popularity rounded!!
	}
	private void initBestBuy() {
		double[] agesPop={10.2,14.1,16.7,20.4,17.4};
		int[] incomePop={47, 26, 18}; //warn rounded
		initPublisher(2, 20, agesPop, incomePop);	//warning : popularity rounded!!
	}
	private void initSears() {
		double[] agesPop={9.2,12.1,16.7,20.4,18.4};
		int[] incomePop={45, 25, 20}; 
		initPublisher(2, 19, agesPop, incomePop);	//warning : popularity rounded!!
		
	}
	private void initWebmd() {
		double[] agesPop={9.2,15.1,15.7,19.4,18.4};
		int[] incomePop={46, 27, 19}; //warn rounded
		initPublisher(3, 24, agesPop, incomePop);	//warning : popularity rounded!!
	}
	private void initEhow() {
		double[] agesPop={10.2,15.1,15.7,19.4,17.4};
		int[] incomePop={50, 27, 15}; 
		initPublisher(3, 29, agesPop, incomePop);	//warning : popularity rounded!!		
	}
	private void initAsk() {
		double[] agesPop={10.2,13.1,15.7,20.4,18.4};
		int[] incomePop={50, 28, 15}; 
		initPublisher(5, 28, agesPop, incomePop);		
	}
	private void initTripadvisor() {
		double[] agesPop={8.2,16.1,17.7,20.4,17.4};
		int[] incomePop={47, 26, 18}; //warn rounded
		initPublisher(2, 30, agesPop, incomePop);	//warning : popularity rounded!!		
	}
	private void initCnet() {
		double[] agesPop={12.2,15.1,15.7,18.4,17.4};
		int[] incomePop={48, 27, 17}; //warn rounded
		initPublisher(2, 27, agesPop, incomePop);	//warning : popularity rounded!!		
	}	
	private void initWeather() {
		double[] agesPop={9.2,15.1,16.7,20.4,18.4};
		int[] incomePop={46, 27, 19}; //warn rounded
		initPublisher(6, 31, agesPop, incomePop);	//warning : popularity rounded!!		
	}
	public Map<Data.Income, Integer> getIncomeOrientation() {
		return incomeOrientation;
	}

	public void setIncomeOrientation(Map<Data.Income, Integer> incomeOrientation) {
		this.incomeOrientation = incomeOrientation;
	}

	public Map<Data.Age, Double> getAgeOrientation(){
		return ageOrientation;
	}
	public void setAgeOrientation(Map<Data.Age, Double> ageOrientation) {
		this.ageOrientation = ageOrientation;
	}
	

	
}
