import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import tau.tac.adx.report.adn.MarketSegment;


public class Population {
	
	HashMap<Set<MarketSegment>, Double> segments = new HashMap<>();
	public void compute_segments(){
		
		/* 3-PARTITIONS :*/
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME), 407.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME), 808.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME), 256.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME), 517.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.LOW_INCOME), 2401.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.LOW_INCOME), 1795.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME), 1980.0);
		segments.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME), 1836.0);
		
		/*2-PARTITION:*/
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.OLD), 2808.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.OLD), 2603.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.YOUNG), 2236.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.YOUNG), 2353.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.HIGH_INCOME),  663.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.HIGH_INCOME), 1325.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE,  MarketSegment.LOW_INCOME), 4381.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE,  MarketSegment.LOW_INCOME), 3631.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.OLD, MarketSegment.HIGH_INCOME), 1215.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.YOUNG, MarketSegment.HIGH_INCOME),  773.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.OLD,  MarketSegment.LOW_INCOME), 4196.0);
		segments.put(MarketSegment.compundMarketSegment2(MarketSegment.YOUNG,  MarketSegment.LOW_INCOME), 3816.0);
		
		/*1-PARTITION*/
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.MALE), 4956.0);
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.FEMALE), 5044.0);
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.LOW_INCOME), 8012.0);
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.HIGH_INCOME), 1988.0);
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.OLD), 5411.0);
		segments.put(MarketSegment.compundMarketSegment1(MarketSegment.YOUNG), 4589.0);
		
	}
	public double getSizeSegment(Set<MarketSegment> targetedSegment){
		return segments.get(targetedSegment);
	}
	
		
}
