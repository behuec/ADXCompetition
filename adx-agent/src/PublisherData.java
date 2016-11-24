import java.util.Map;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;


public class PublisherData extends AdxPublisherReportEntry{
	
	private double popularity;
	private Map<AdType, Integer>  AdOrientation; //video or text
	private double reservePriceBaseline;
	private Map<Data.Age, Double> AgeOrientation; //Initial Stats
	private Map<Data.Income, Double> IncomeOrientation; //Initial Stats
	
	public Map<Data.Income, Double> getIncomeOrientation() {
		return IncomeOrientation;
	}

	public void setIncomeOrientation(Map<Data.Income, Double> incomeOrientation) {
		IncomeOrientation = incomeOrientation;
	}

	public Map<Data.Age, Double> getAgeOrientation(){
		return AgeOrientation;
	}
	public void setAgeOrientation(Map<Data.Age, Double> ageOrientation) {
		AgeOrientation = ageOrientation;
	}
}
