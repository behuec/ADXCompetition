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
		for(int i = 0; i<60; i++)
			competition.add(new HashMap<Set<MarketSegment>, Double>());
	}
	
	public void addCampaign(CampaignData camp){
		int duration 		= (int)( camp.dayEnd - camp.dayStart );
		double reach 		= (double) camp.reachImps.intValue();
		double reachPerDay 	= reach / duration; // Heuristic: Assume the reach is linearly distributed
		double targetSize   = Population.getSizeSegment(camp.targetSegment); // get size of the targeted segment
		// compute the list of triplets that constitute the target
		ArrayList<Set<MarketSegment>> segments = Population.getSection(camp.targetSegment); 
		
		for(int i = (int)camp.dayStart; i <= camp.dayEnd; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			double newValue;
			for(Set<MarketSegment> segment : segments){
				double tripletPercentage = Population.getSizeSegment(segment) / targetSize;
				if(competitionDay.containsKey(segment))
					newValue = competitionDay.get(segment) + reachPerDay*tripletPercentage;
				else
					newValue = reachPerDay*tripletPercentage;
				
				competitionDay.put(segment, newValue);
			}
		}
	}
	
	public double computeCompetition(int startDay, int endDay, Set<MarketSegment> targetSegment){
		return 0.0;
	}
	
	// return the competition as the percentage of the segment still available
	public double getCompetition(int startDay, int endDay, Set<MarketSegment> targetSegment){
		double totalDemand = 0;
		double avgSegment = Population.getSizeSegment(targetSegment);
		for(int i = startDay; i <= endDay; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			if( competitionDay.get(targetSegment) != null )
				totalDemand += competitionDay.get(targetSegment);
		}
		// if totalDemand = 0 			: returns 1  | There is no competition
		// if totalDemand = avgSegment	: returns 0  | The segment is full
		// if totalDemand > avgSegment	: returns -x | Abs(x) is the percentage of overshoot
		return ( avgSegment - totalDemand ) / avgSegment; 
	}
	
	public void updateCompetition(Set<MarketSegment> segment, int currDay, int endDay, double impressions){
		int duration = (endDay - currDay); // Check remaning duration
		// Compute the progress as the difference between the linear estimation for today and the actual number that we got.
		// Update the tracker by distributing the remainder over the rest of the campaign ( if we overshoot progress < 0 )
		double progress = (competition.get(currDay).get(segment) - impressions) / duration; 
		
		for(int i = currDay + 1; i <= endDay; i++){
			HashMap<Set<MarketSegment>,Double> info_day = competition.get(i);
			double old_value = info_day.get(segment);
			if(old_value + progress > 0)
				info_day.put(segment, old_value + progress);
			else 
				info_day.remove(segment);
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
