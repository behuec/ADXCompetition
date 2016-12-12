import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import tau.tac.adx.report.adn.MarketSegment;


public class Population {
	
	static HashMap<Set<MarketSegment>, Double> segments = new HashMap<>();
	static HashSet<MarketSegment> age = new HashSet<MarketSegment>();
	static HashSet<MarketSegment> sex = new HashSet<MarketSegment>();
	static HashSet<MarketSegment> income = new HashSet<MarketSegment>();
	
	//Each partition (1-partition, 2-partition or 3-partition) is equivalent to a set of 3-partitions
	static HashMap<Set<MarketSegment>,ArrayList<Set<MarketSegment>>> sets = new HashMap<Set<MarketSegment>,ArrayList<Set<MarketSegment>>>();
	
	public Population(){
		
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
				
		ArrayList<Set<MarketSegment>> M = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> F = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> O = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> Y = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> L = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> H = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MY = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MO = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FY = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FO = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> ML = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> YH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> OH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> YL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> OL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FOH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MOH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FYH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MYH = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FOL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MOL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> FYL = new ArrayList<Set<MarketSegment>>();
		ArrayList<Set<MarketSegment>> MYL = new ArrayList<Set<MarketSegment>>();
		
		Set<MarketSegment> foh = MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME);
		Set<MarketSegment> moh = MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME);
		Set<MarketSegment> fyh = MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME);
		Set<MarketSegment> myh = MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME);
		Set<MarketSegment> fol = MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.LOW_INCOME);
		Set<MarketSegment> mol = MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.LOW_INCOME);
		Set<MarketSegment> fyl = MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME);
		Set<MarketSegment> myl = MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME);
		
		M.add(moh);	M.add(myh); M.add(mol); M.add(myl);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.MALE),M);
		F.add(foh); F.add(fyh); F.add(fol); F.add(fyl);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.FEMALE),F);
		O.add(moh); O.add(foh); O.add(mol); O.add(fol);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.OLD),O);
		Y.add(myh); Y.add(fyh); Y.add(myl); Y.add(fyl);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.YOUNG),Y);
		L.add(mol); L.add(fol); L.add(myl); L.add(fyl);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.LOW_INCOME),L);
		H.add(moh); H.add(foh); H.add(myh); H.add(fyh);
		sets.put(MarketSegment.compundMarketSegment1(MarketSegment.HIGH_INCOME),H);
		
		FOH.add(foh);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME),FOH);
		MOH.add(moh);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.HIGH_INCOME),MOH);
		FYH.add(fyh);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME),FYH);
		MYH.add(myh);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.HIGH_INCOME),MYH);
		FOL.add(fol);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.OLD, MarketSegment.LOW_INCOME),FOL);
		MOL.add(mol);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.OLD, MarketSegment.LOW_INCOME),MOL);
		FYL.add(fyl);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME),FYL);
		MYL.add(myl);
		sets.put(MarketSegment.compundMarketSegment3(MarketSegment.MALE, MarketSegment.YOUNG, MarketSegment.LOW_INCOME),MYL);
		
		FO.add(fol); FO.add(foh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.OLD), FO);
		MO.add(mol); MO.add(moh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.OLD), MO);
		FY.add(fyl); FY.add(fyh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.YOUNG), FY);
		MY.add(myl); MY.add(myh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.YOUNG), MY);
		FH.add(fyh); FH.add(foh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE, MarketSegment.HIGH_INCOME),  FH);
		MH.add(myh); MH.add(moh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE, MarketSegment.HIGH_INCOME), MH);
		FL.add(fyl); FL.add(fol);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.FEMALE,  MarketSegment.LOW_INCOME), FL);
		ML.add(myl); ML.add(mol);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.MALE,  MarketSegment.LOW_INCOME), ML);
		OH.add(foh); OH.add(moh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.OLD, MarketSegment.HIGH_INCOME), OH);
		YH.add(fyh); YH.add(myh);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.YOUNG, MarketSegment.HIGH_INCOME),  YH);
		OL.add(fol); OL.add(mol);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.OLD,  MarketSegment.LOW_INCOME), OL);
		YL.add(fyl); YL.add(myl);
		sets.put(MarketSegment.compundMarketSegment2(MarketSegment.YOUNG,  MarketSegment.LOW_INCOME), YL);
		
		//compute the avg imp per day per segment:
		computeAVGimps();
	}
	//compute the average single impressions each day on each segment 
	private static void computeAVGimps(){
		double avg = Data.NContinueMax * Math.pow(Data.PContinue, Data.NContinueMax);
		for(int i =1; i<Data.NContinueMax-1; i++){
			avg+= i* Math.pow(Data.PContinue, i) * (1-Data.PContinue);
		}
		for (Entry<Set<MarketSegment>, Double> seg : ((HashMap<Set<MarketSegment>, Double>) segments.clone()).entrySet()){
			//geometric law ("probability of reject") + each user is going at least once on website.
			segments.put(seg.getKey(), seg.getValue()*avg+seg.getValue());
		}
		
	}

	public static double getSizeSegment(Set<MarketSegment> targetedSegment){
		return segments.get(targetedSegment);
	}
	
	public static ArrayList<Set<MarketSegment>> getTriplets(Set<MarketSegment> targetedSegment){
		return sets.get(targetedSegment);
	}
		
}
