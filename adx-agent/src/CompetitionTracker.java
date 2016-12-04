import java.util.ArrayList;
import java.util.HashMap;
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
		competition = new ArrayList<HashMap<Set<MarketSegment>,Integer>>(60);
	}
	
	public void addCampaign(CampaignData camp){
		int duration 		= (int)( camp.dayEnd - camp.dayEnd );
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
}
