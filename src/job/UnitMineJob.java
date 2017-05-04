package job;

import bwapi.Match;
import bwapi.Position;
import bwapi.Unit;
import bwapi.WeaponType;
import bwta.BWTA;
import extension.BWAPIHelper;

public class UnitMineJob extends UnitJob {
	
	public Unit mineralField;
	
	private Unit lastMineralField;
	
	public UnitMineJob(Unit unit, Unit mineralField) {
		super(unit);
		this.mineralField = mineralField;
		this.lastMineralField = mineralField;
	}

	@Override
	public void perform() {
			
		// Enemy units nearby
		Unit enemy = BWAPIHelper.getNearestEnemyUnit(unit.getPosition(), null);
		if (enemy != null){
			if (enemy.getType().isWorker()
					&& unit.getDistance(enemy) < 100 
					&& BWTA.getNearestBaseLocation(unit.getPosition()).getPosition().getDistance(unit.getPosition()) < 200){
				if (Match.getInstance().getFrameCount() % 10 == 0){
					unit.attack(enemy);
				}
				return;
			} else {
				WeaponType weapon = BWAPIHelper.getWeaponAgainst(enemy, unit); 
				if (weapon != null && unit.getDistance(enemy) <= weapon.maxRange() * 2){
					Position position = BWAPIHelper.getKitePosition(unit, enemy, weapon.maxRange());
					unit.move(position);
					return;
				} else if (enemy.getType().isSpellcaster() && unit.getDistance(enemy) <= 12){
					Position position = BWAPIHelper.getKitePosition(unit, enemy, weapon.maxRange());
					unit.move(position);
					return;
				}
			}
		}
		
		// Else - gather minerals
		if (unit.isGatheringMinerals()){
			if (lastMineralField.getID() != mineralField.getID()){
				lastMineralField = mineralField;
				unit.gather(this.mineralField);
			}
			return;
		}
		
		if (unit.isCarryingMinerals()){
			unit.returnCargo();
			return;
		}
		
		unit.gather(this.mineralField);
		
	}
	
	@Override
	public String toString() {
		return "Mine";
	}
	
}
