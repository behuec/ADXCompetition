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
	ArrayList<HashMap<Set<MarketSegment>,Integer>> competition;
	
	public CompetitionTracker(){
		competition = new ArrayList<HashMap<Set<MarketSegment>,Integer>>();
		for(int i = 0; i<60; i++)
			competition.add(new HashMap<Set<MarketSegment>, Integer>());
	}
	
	public void addCampaign(CampaignData camp){
		int duration 		= (int)( camp.dayEnd - camp.dayStart );
		int reach 			= camp.reachImps.intValue();
		int reachPerDay 	= reach / duration; // Heuristic: Assume the reach is linearly distributed 
		int remainder 		= reach % duration; // remainder days will be increased by one
		Set<MarketSegment> segment = camp.targetSegment;
		for(int i = (int)camp.dayStart; i <= camp.dayEnd; i++){
			HashMap<Set<MarketSegment>,Integer> competitionDay = competition.get(i);
			int newValue;
			
			if(competitionDay.containsKey(segment))
				newValue = competitionDay.get(segment).intValue() + reachPerDay;
			else
				newValue = reachPerDay;
			
				newValue = newValue + ((remainder-- > 0) ? 1 : 0 ); // spread the remainder across the days 
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
			HashMap<Set<MarketSegment>,Integer> competitionDay = competition.get(i);
			totalDemand += competitionDay.get(targetSegment);
		}
		return (double) budget / (double) totalDemand; 
	}
	
	public void updateCompetition(Set<MarketSegment> segment, int currDay, int endDay, int impressions){
		for(int i = currDay; i <= endDay; i++){
			
		}
	}
	
	public void competitionStats(String type, int day ){
		for(int i = day; i<60 && i<day+3; i++){
			HashMap<Set<MarketSegment>,Integer> info_day = competition.get(i);
			System.out.println("\n"+type+" competition on day:"+i);
			for(Map.Entry<Set<MarketSegment>,Integer> info : info_day.entrySet()){
				System.out.print(" " + info.getKey() + " : " + info.getValue());
			}
		}
		System.out.println();
	}
}
