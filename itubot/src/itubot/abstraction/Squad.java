package itubot.abstraction;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.Unitset;
import itubot.bot.ITUBot;
import itubot.bwapi.Match;
import itubot.bwapi.Self;
import itubot.job.UnitCombatJob;
import itubot.log.BotLogger;
import itubot.manager.assualt.AssaultManager;
import itubot.manager.assualt.CombatPredictor;
import itubot.manager.information.InformationManager;
import itubot.manager.squad.SquadManager;

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
		if (ITUBot.getInstance().informationManager.getEnemyBaseLocation() != null){
			target = ITUBot.getInstance().informationManager.getEnemyBaseLocation().getPosition();
		}
		text = "Idle";
	}
	
	public void add(Unit unit) {
		if (unit == null){
			BotLogger.getInstance().log(this, "Unit is null");
		} else if (target != null){
			assignments.add(new UnitAssignment(unit, new UnitCombatJob(unit, target, true)));
		} else {
			assignments.add(new UnitAssignment(unit, null));
		}
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
		if (ITUBot.getInstance().informationManager.getEnemyBaseLocation() != null){

			// Target enemy base
			target = ITUBot.getInstance().assualtManager.getTarget(this);
			
			// Attack or merge
			if (target == null && ITUBot.getInstance().squadManager.getSquads().size() == 1){
				Position home = ITUBot.getInstance().assualtManager.getRallyPoint();
				text = "Retreating";
				for (UnitAssignment assignment : assignments){
					if (assignment.job instanceof UnitCombatJob)
						((UnitCombatJob)assignment.job).target = home;
					else
						assignment.job = new UnitCombatJob(assignment.unit, home, false);
				}
			} else if (target == null){
				int largest = 0;
				Squad largestSquad = null;
				for(Squad squad : ITUBot.getInstance().squadManager.getSquads()){
					if (squad.id != this.id){
						if (squad.assignments.size() > largest){
							largest = squad.assignments.size();
							largestSquad = squad;
						}
					}
				}
				Position center = largestSquad.getCenter();
				text = "Merging " + center;
				for (UnitAssignment assignment : assignments){
					if (assignment.job instanceof UnitCombatJob){
						((UnitCombatJob)assignment.job).target = center;
						((UnitCombatJob)assignment.job).attack = false;
					}else{
						assignment.job = new UnitCombatJob(assignment.unit, center, false);
					}
				}
			} else {
				text = "Attacking " + target;
				for(UnitAssignment assignment : assignments){
					if (assignment.job != null && assignment.job instanceof UnitCombatJob){
						((UnitCombatJob)assignment.job).target = target;
						((UnitCombatJob)assignment.job).attack = true;
					} else if (assignment.unit != null){
						if (assignment.unit == null){
							BotLogger.getInstance().log(this, "Unit is null");
						}
						assignment.job = new UnitCombatJob(assignment.unit, target, true);
					} else {
						BotLogger.getInstance().log(this, "Unit is null");
					}
				}
			}
		}
	}

	public int mergeDistance() {
		return (int)(64 + (32 * Math.pow(assignments.size(), 0.5) ));
	}
	
	public int splitDistance() {
		return (int)(256 + (32 * Math.pow(assignments.size(), 0.75) ));
	}

}
