package itubot.job;


import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import itubot.bwapi.Match;
import itubot.exception.NoBaseLocationsLeftException;
import itubot.exception.NoWorkersException;
import itubot.log.BotLogger;
import itubot.manager.BuildLocationManager;

public class UnitBuildJob extends UnitJob {

	public UnitType unitType;
	public TilePosition position;
	
	public UnitBuildJob(Unit unit, UnitType unitType, TilePosition position) {
		super(unit);
		this.position = position;
		this.unitType = unitType;
	}

	public void perform() throws NoWorkersException, NoBaseLocationsLeftException {

		if (position == null){
			return;
		}
		if (unit.getDistance(position.toPosition()) > unit.getType().sightRange()){
			unit.move(position.toPosition());
		} else {
			UnitType test = getTestBuild();
			if (Match.getInstance().canBuildHere(position, test, unit)){
				Match.getInstance().drawTextMap(position.toPosition(), "("+position.getX()+","+position.getY() + ")");
				Match.getInstance().drawBoxMap(new Position(position.toPosition().getX(), position.toPosition().getY()), new Position(position.toPosition().getX() + 32, position.toPosition().getY()+32), Color.Green);
				unit.build(this.unitType, this.position);
			} else {
				unit.move(position.toPosition());
			}
		}
	}
	
	private UnitType getTestBuild() {
		if (unitType == UnitType.Protoss_Citadel_of_Adun || unitType == UnitType.Protoss_Templar_Archives){
			return UnitType.Protoss_Cybernetics_Core;
		}
		return unitType;
	}

	@Override
	public String toString() {
		return "Build " + unitType;
	}
	
}