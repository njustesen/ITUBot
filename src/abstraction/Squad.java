package abstraction;

import java.util.ArrayList;
import java.util.List;

import bwapi.Match;
import bwapi.Position;
import bwapi.Self;
import bwapi.Unit;
import bwapi.Unitset;
import job.UnitAttackJob;
import job.UnitRetreatJob;
import manager.InformationManager;
import manager.SquadManager;
import module.CombatPredictor;

public class Squad {

	private static int created = 0;
	
	public List<UnitAssignment> assignments;
	public Position target;
	public int id;
	public String text;
	
	public Squad() {
		super();
		this.assignments = new ArrayList<UnitAssignment>();
		this.target = null;
		this.id = created+1;
		created++;
		if (InformationManager.getInstance().enemyBaseLocation != null){
			target = InformationManager.getInstance().enemyBaseLocation.getPosition();
		}
		text = "Idle";
	}
	
	public void add(Unit unit) {
		if (target != null)
			assignments.add(new UnitAssignment(unit, new UnitAttackJob(target)));
		else
			assignments.add(new UnitAssignment(unit, null));
	}
	
	public void remove(Unit unit) {
		int idx = -1;
		int i = 0;
		for(UnitAssignment assignment : assignments){
			if (assignment.unit.getID() == unit.getID()){
				idx = i;
				break;
			}
			i++;
		}
		assignments.remove(idx);
	}

	public Position getCenter() {
		double x = 0;
		double y = 0;
		for (UnitAssignment assignment : this.assignments){
			x += assignment.unit.getPosition().getX();
			y += assignment.unit.getPosition().getY();
		}
		x = x / (double)this.assignments.size();
		y = y / (double)this.assignments.size();
		return new Position((int)x, (int)y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Squad other = (Squad) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void control() {
		assignJobs();
		for(UnitAssignment assignment : assignments){
			assignment.perform();
		}
	}

	private void assignJobs() {
		// Adjust target - Attack if only one possible base location
		if (InformationManager.getInstance().enemyBaseLocation != null){

			// Target enemy base
			target = InformationManager.getInstance().enemyBaseLocation.getPosition();
			
			// Estimate win change
			double score = CombatPredictor.getInstance().prediction(this, target, 1.33);
			
			// Retreat or merge
			if (score < 0){
				if (SquadManager.getInstance().squads.size() < 2){
					Position home = InformationManager.getInstance().ownMainBaseLocation.getPosition();
					text = "Retreating (" + score + ") " + home;
					for (UnitAssignment assignment : assignments){
						if (assignment.job instanceof UnitRetreatJob)
							((UnitRetreatJob)assignment.job).target = home;
						else
							assignment.job = new UnitRetreatJob(home);
					}
				} else {
					double closest = Double.MAX_VALUE;
					Position closestSquad = this.getCenter();
					for(Squad squad : SquadManager.getInstance().squads){
						if (squad.id != this.id){
							Position otherCenter = squad.getCenter();
							double distance = target.getDistance(otherCenter);
							if (distance < closest){
								closest = distance;
								closestSquad = otherCenter;
							}
						}
					}
					text = "Merging (" + score + ") " + closestSquad;
					for (UnitAssignment assignment : assignments){
						if (assignment.job instanceof UnitRetreatJob)
							((UnitRetreatJob)assignment.job).target = closestSquad;
						else
							assignment.job = new UnitRetreatJob(closestSquad);
					}
				}
			} else {
				text = "Attacking (" + score + ") " + target;
				// assign jobs
				for(UnitAssignment assignement : assignments){
					if (assignement.job != null && assignement.job instanceof UnitAttackJob){
						((UnitAttackJob)assignement.job).target = target;
					} else {
						assignement.job = new UnitAttackJob(target);
					}
				}
			}
		}
	}
	
	

}
