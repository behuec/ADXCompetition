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
		Set<MarketSegment> segment = camp.targetSegment;
		for(int i = (int)camp.dayStart; i <= camp.dayEnd; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			double newValue;
			
			if(competitionDay.containsKey(segment))
				newValue = competitionDay.get(segment) + reachPerDay;
			else
				newValue = reachPerDay;
			
			competitionDay.put(segment, newValue);
		}
	}
	
	/*
	 * Calculates how many impressions the opponents need in that same segment over the specified 
	 * period and compares it to my budget.
	 * ex 1.0 if no competition, 0.5 if the other need as many impressions as I do and so on
	 */
	public double getCompetitionFactorForBudget(int startDay, int endDay, Set<MarketSegment> targetSegment, int budget){
		int totalDemand = budget;
		for(int i = startDay; i<= endDay; i++){
			HashMap<Set<MarketSegment>,Double> competitionDay = competition.get(i);
			totalDemand += competitionDay.get(targetSegment);
		}
		return (double) budget / (double) totalDemand; 
	}
	
	public void updateCompetition(Set<MarketSegment> segment, int currDay, int endDay, double impressions){
		int duration = (endDay - currDay);
		double chunk = impressions / duration;
		
		for(int i = currDay; i <= endDay; i++){
			HashMap<Set<MarketSegment>,Double> info_day = competition.get(i);
			double old_value = info_day.get(segment);
			info_day.put(segment, old_value - chunk);
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
