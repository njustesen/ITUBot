package itubot.manager.buildlocation;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import itubot.bot.ITUBot;
import itubot.bwapi.Match;
import itubot.bwapi.Self;
import itubot.exception.ITUBotException;
import itubot.exception.NoSpaceLeftForBuildingException;
import itubot.exception.NoWorkersException;
import itubot.manager.information.InformationManager;

public class BruteBuildLocator implements IBuildLocationManager {

	public BruteBuildLocator(){
		
	}
	
	public TilePosition getLocation(UnitType buildingType) throws NoWorkersException, NoSpaceLeftForBuildingException{
		
		// Get random worker
		Unit someWorker = null;
		for (Unit u : Match.getInstance().getAllUnits()) {
			if (u.getPlayer().equals(Match.getInstance().self()) && u.canBuild()){
				someWorker = u;
			}
		}
		if (someWorker == null){
			throw new NoWorkersException();
		}
		
		// Refinery, Assimilator, Extractor
		int stopDist = 100;
		TilePosition aroundTile = Match.getInstance().self().getStartLocation();
		if (buildingType.isRefinery()) {
			for (Unit n : Match.getInstance().neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
						( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
						( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
						) return n.getTilePosition();
			}
		}
		
		Region region = BWTA.getRegion(ITUBot.getInstance().informationManager.getOwnMainBaseLocation().getTilePosition());
		TilePosition position = bestInRegion(region, buildingType, someWorker);
		if (position == null){
			for(Region r : region.getReachableRegions()){
				position = bestInRegion(r, buildingType, someWorker);
				if (position != null){
					return position;
				}
			}
		}
		
		throw new NoSpaceLeftForBuildingException();
		
	}
	
	private TilePosition bestInRegion(Region region, UnitType buildingType, Unit someWorker){
		// Iterate all tiles in region
		for (int x = 0; x < Match.getInstance().mapWidth(); x++){
			for (int y = 0; y < Match.getInstance().mapHeight(); y++){
				TilePosition position = new TilePosition(x, y);
				if (BWTA.getRegion(position).equals(region)){
					if (Match.getInstance().canBuildHere(position, buildingType, someWorker, false)){
						return position;
					}
				}
			}
		}
		return null;
	}

	private double getScore(UnitType buildingType, TilePosition position) {
		if (buildingType.equals(UnitType.Protoss_Pylon)){
			return pylonScore(buildingType, position);
		} else if (buildingType.equals(UnitType.Protoss_Photon_Cannon)){
			return cannonScore(buildingType, position);
		} else if (buildingType.equals(UnitType.Protoss_Nexus)){
			return cannonScore(buildingType, position);
		}
		return 0;
	}

	private double pylonScore(UnitType buildingType, TilePosition position) {
		double pylonSum = 0;
		int pylonCount = 0;
		double mineSum = 0;
		int mineCount = 0;
		double gasSum = 0;
		int gasCount = 0;
		double closestNexus = Integer.MAX_VALUE;
		
		for(Unit unit : Match.getInstance().getAllUnits()){
			if (unit.getPlayer().getID() == Self.getInstance().getID()){
				if (unit.getType().equals(UnitType.Protoss_Pylon)){
					pylonSum += unit.getDistance(position.toPosition());
					pylonCount++;
				} else if (unit.getType().equals(UnitType.Protoss_Nexus)){
					closestNexus = Math.min(closestNexus, unit.getDistance(position.toPosition()));
				} else if (unit.getType().equals(UnitType.Protoss_Assimilator)){
					gasSum += unit.getDistance(position.toPosition());
					gasCount++;
				}
			} else if (unit.getType().equals(UnitType.Resource_Mineral_Field) || unit.getType().equals(UnitType.Resource_Mineral_Field_Type_2) ||
					unit.getType().equals(UnitType.Resource_Mineral_Field_Type_3)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				mineSum += unit.getDistance(position.toPosition());
				mineCount++;
			} else if (unit.getType().equals(UnitType.Resource_Vespene_Geyser)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				gasSum += unit.getDistance(position.toPosition());
				gasCount++;
			}
		}
		
		double mineAvg = (mineCount == 0 ? 0 : mineSum / mineCount);
		double pylonAvg = (pylonCount == 0 ? 0 : pylonSum / pylonCount);
		double gasAvg = (gasCount == 0 ? 0 : gasSum / gasCount);
		double nexusScore = -closestNexus;
		
		return nexusScore*2 + pylonAvg/20 + mineAvg + gasAvg;
	}
	
	private double buildingScore(UnitType buildingType, TilePosition position) {
		double pylonSum = 0;
		int pylonCount = 0;
		double mineSum = 0;
		int mineCount = 0;
		double gasSum = 0;
		int gasCount = 0;
		double closestNexus = Integer.MAX_VALUE;
		
		for(Unit unit : Match.getInstance().getAllUnits()){
			if (unit.getPlayer().getID() == Self.getInstance().getID()){
				if (unit.getType().equals(UnitType.Protoss_Pylon)){
					pylonSum += unit.getDistance(position.toPosition());
					pylonCount++;
				} else if (unit.getType().equals(UnitType.Protoss_Nexus)){
					closestNexus = Math.min(closestNexus, unit.getDistance(position.toPosition()));
				} else if (unit.getType().equals(UnitType.Protoss_Assimilator)){
					gasSum += unit.getDistance(position.toPosition());
					gasCount++;
				}
			} else if (unit.getType().equals(UnitType.Resource_Mineral_Field) || unit.getType().equals(UnitType.Resource_Mineral_Field_Type_2) ||
					unit.getType().equals(UnitType.Resource_Mineral_Field_Type_3)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				mineSum += unit.getDistance(position.toPosition());
				mineCount++;
			} else if (unit.getType().equals(UnitType.Resource_Vespene_Geyser)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				gasSum += unit.getDistance(position.toPosition());
				gasCount++;
			}
		}
		
		double mineAvg = (mineCount == 0 ? 0 : mineSum / mineCount);
		double gasAvg = (gasCount == 0 ? 0 : gasSum / gasCount);
		double pylonAvg = -(pylonCount == 0 ? 0 : pylonSum / pylonCount);
		double nexusScore = -closestNexus;
		
		return nexusScore + pylonAvg + mineAvg*1.5 + gasAvg*1.5;
	}
	
	private double cannonScore(UnitType buildingType, TilePosition position) {
		double cannonSum = 0;
		int cannonCount = 0;
		double mineSum = 0;
		int mineCount = 0;
		double gasSum = 0;
		int gasCount = 0;
		double closestNexus = Integer.MAX_VALUE;
		
		for(Unit unit : Match.getInstance().getAllUnits()){
			if (unit.getPlayer().getID() == Self.getInstance().getID()){
				if (unit.getType().equals(UnitType.Protoss_Photon_Cannon)){
					cannonSum += unit.getDistance(position.toPosition());
					cannonCount++;
				} else if (unit.getType().equals(UnitType.Protoss_Nexus)){
					closestNexus = Math.min(closestNexus, unit.getDistance(position.toPosition()));
				} else if (unit.getType().equals(UnitType.Protoss_Assimilator)){
					gasSum += unit.getDistance(position.toPosition());
					gasCount++;
				}
			} else if (unit.getType().equals(UnitType.Resource_Mineral_Field) || unit.getType().equals(UnitType.Resource_Mineral_Field_Type_2) ||
					unit.getType().equals(UnitType.Resource_Mineral_Field_Type_3)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				mineSum += unit.getDistance(position.toPosition());
				mineCount++;
			} else if (unit.getType().equals(UnitType.Resource_Vespene_Geyser)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				gasSum += unit.getDistance(position.toPosition());
				gasCount++;
			}
		}
		
		double mineAvg = (mineCount == 0 ? 0 : mineSum / mineCount);
		double cannonAvg = (cannonCount == 0 ? 0 : cannonSum / cannonCount);
		double gasAvg = (gasCount == 0 ? 0 : gasSum / gasCount);
		double nexusScore = -closestNexus;
		
		return nexusScore*3 + cannonAvg*2 + mineAvg/2 + gasAvg/2;
	}
	
	private double nexusScore(UnitType buildingType, TilePosition position) {
		double mineSum = 0;
		int mineCount = 0;
		double gasSum = 0;
		int gasCount = 0;
		double closestNexus = Integer.MAX_VALUE;
		
		for(Unit unit : Match.getInstance().getAllUnits()){
			if (unit.getPlayer().getID() == Self.getInstance().getID()){
				if (unit.getType().equals(UnitType.Protoss_Nexus)){
					if (unit.getDistance(position.toPosition()) < 600){
						break;
					}
					closestNexus = Math.min(closestNexus, unit.getDistance(position.toPosition()));
				} else if (unit.getType().equals(UnitType.Protoss_Assimilator)){
					gasSum += unit.getDistance(position.toPosition());
					gasCount++;
				}
			} else if (unit.getType().equals(UnitType.Resource_Mineral_Field) || unit.getType().equals(UnitType.Resource_Mineral_Field_Type_2) ||
					unit.getType().equals(UnitType.Resource_Mineral_Field_Type_3)){
				mineSum += unit.getDistance(position.toPosition());
				mineCount++;
			} else if (unit.getType().equals(UnitType.Resource_Vespene_Geyser)){
				if (unit.getDistance(position.toPosition()) > 1000){
					break;
				}
				gasSum += unit.getDistance(position.toPosition());
				gasCount++;
			}
		}
		
		double mineAvg = -(mineCount == 0 ? 0 : mineSum / mineCount);
		double gasAvg = -(gasCount == 0 ? 0 : gasSum / gasCount);
		double nexusScore = -closestNexus;
		
		return nexusScore + mineAvg*10000 + gasAvg*10000;
	}

	@Override
	public void execute() throws ITUBotException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visualize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnd(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNukeDetect(Position arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerDropped(Player arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerLeft(Player arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveText(Player arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSaveGame(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSendText(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitComplete(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitCreate(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitDestroy(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitDiscover(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitEvade(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitHide(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitMorph(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitRenegade(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitShow(Unit arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFree(TilePosition position, UnitType buildingType) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
