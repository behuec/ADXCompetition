import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tau.tac.adx.report.adn.MarketSegment;

public class CompetitionTracker {
	/*
	* Ugliest data structure I've ever seen used to keep track of the competition between contracts.
	* ForEach day of the competition (60) :
	* 	contains a mapping ( HashMap ) of segments (Set<MarketSegment>) and impressions (Integer)
	*/
	ArrayList<HashMap<Set<MarketSegment>,Double>> competition;
	
	public CompetitionTracker(){
		competition = new ArrayList<HashMap<Set<MarketSegment>,Double>>();
		for(int i = 0; i < Data.TGamedays; i++)
			competition.add(new HashMap<Set<MarketSegment>, Double>());
	}
	
	public void addCampaign(CampaignData camp){
		int duration 		= (int)( camp.dayEnd - camp.dayStart );
		double reach 		= (double) camp.reachImps.intValue();
		double reachPerDay 	= reach / duration; // Heuristic: Assume the reach is linearly distributed
		double targetSize   = Population.getSizeSegment(camp.targetSegment); // get size of the targeted segment
		// compute the list of triplets that constitute the target
		ArrayList<Set<MarketSegment>> triplets = Population.getTriplets(camp.targetSegment); 
		
		for(int i = (int)camp.dayStart; i <= camp.dayEnd; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			double dailyReachPerTriplet;
			for(Set<MarketSegment> triplet : triplets){
				double tripletPercentage = Population.getSizeSegment(triplet) / targetSize;
				if(competitionDay.containsKey(triplet))
					dailyReachPerTriplet = competitionDay.get(triplet) + reachPerDay*tripletPercentage;
				else
					dailyReachPerTriplet = reachPerDay*tripletPercentage;
				
				competitionDay.put(triplet, dailyReachPerTriplet);
			}
		}
	}
	
	public double computeCompetition(int startDay, int endDay, Set<MarketSegment> targetSegment){
		return 0.0;
	}
	
	// return the competition as the percentage of the segment still available
	public double getCompetition(int startDay, int endDay, Set<MarketSegment> targetSegment){
		double totalDemand = 0;
		double segmentSize = Population.getSizeSegment(targetSegment);
		ArrayList<Set<MarketSegment>> triplets = Population.getTriplets(targetSegment);
		for(int i = startDay; i <= endDay; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			for(Set<MarketSegment> triplet : triplets)
			if( competitionDay.get(targetSegment) != null )
				totalDemand += competitionDay.get(targetSegment);
		}
		// if totalDemand = 0 			: returns 1  | There is no competition
		// if totalDemand = avgSegment	: returns 0  | The segment is full
		// if totalDemand > avgSegment	: returns -x | Abs(x) is the percentage of overshoot
		return ( segmentSize - totalDemand ) / segmentSize; 
	}
	
	public void updateCompetition(Set<MarketSegment> segment, int yesterDay, int endDay, double impressionsAchievedYesterday){
		int duration = (endDay - yesterDay ); // Check remaning duration
		// Compute the progress as the difference between the linear estimation for today and the actual number that we got.
		// Update the tracker by distributing the remainder over the rest of the campaign ( if we overshoot progress < 0 )
		double segmentSize = Population.getSizeSegment(segment);
		ArrayList<Set<MarketSegment>> triplets = Population.getTriplets(segment);
		for(int i = yesterDay + 1 ; i <= endDay; i++){
			HashMap<Set<MarketSegment>,Double> info_day = competition.get(i);
			for(Set<MarketSegment> triplet : triplets){
				double tripletPercent = Population.getSizeSegment(triplet) / segmentSize;
				double tripletProgress = impressionsAchievedYesterday * tripletPercent;
				double progress = (competition.get(yesterDay).get(triplet) - tripletProgress) / duration;
				double old_value = info_day.get(triplet);
				if(old_value + progress > 0)
					info_day.put(triplet, old_value + progress);
				else 
					info_day.remove(triplet);
			}
		}
	}
	
	public void competitionStats(String type, int day ){
		for(int i = day; i<60 && i<day+3; i++){
			HashMap<Set<MarketSegment>,Double> info_day = competition.get(i);
			System.out.println("\n"+type+" competition on day:"+i);
			for(Map.Entry<Set<MarketSegment>,Double> info : info_day.entrySet()){
				System.out.print(" " + info.getKey() + " : " + info.getValue());
			}
		}
		System.out.println();
	}
}
