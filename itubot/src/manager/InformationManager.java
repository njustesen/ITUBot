package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.Observation;
import abstraction.UnitAssignment;
import bot.ITUBot;
import bwapi.BWEventListener;
import bwapi.Color;
import bwapi.Enemy;
import bwapi.Match;
import bwapi.Player;
import bwapi.Position;
import bwapi.Self;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import exception.NoBuildOrderException;
import exception.NoWorkersException;
import extension.BWAPIHelper;
import extension.TechTypes;
import extension.UpgradeTypes;
import log.BotLogger;
import module.CombatPredictor;
import module.GasPrioritizor;

public class InformationManager implements Manager, BWEventListener {

	// SINGLETON PATTERN
	private static InformationManager instance = null;
	
	public static InformationManager getInstance() {
	   if(instance == null) {
		   instance = new InformationManager();
	   }
	   return instance;
	}
	
	public static void reset() {
		instance = null;
	}
	
	// CLASS
	public List<BaseLocation> possibleEnemyBasePositions;
	public BaseLocation ownMainBaseLocation;
	public List<BaseLocation> ownBaseLocations;
	public List<Observation> observations;
	public BaseLocation enemyBaseLocation;
	
	public List<Unit> refineries;
	public List<Unit> refineriesInProd;
	
	public List<Unit> bases;
	public List<Unit> pylons;
	
	public Player enemy;
	
	private Map<UnitType, Integer> ownUnitsInProduction;
	private Map<UnitType, Integer> ownUnits;
	private Map<UnitType, Integer> oppUnits;
	
	private HashMap<UpgradeType, Integer> ownUpgrades;
	private HashMap<UpgradeType, Integer> ownUpgradesInProduction;
	private HashMap<TechType, Integer> ownTechs;
	private HashMap<TechType, Integer> ownTechsInProduction;
	
	protected InformationManager(){
		this.ownUnits = new HashMap<UnitType, Integer>();
		this.ownUnitsInProduction = new HashMap<UnitType, Integer>();
		this.ownUpgrades = new HashMap<UpgradeType, Integer>();
		this.ownUpgradesInProduction = new HashMap<UpgradeType, Integer>();
		this.ownTechs = new HashMap<TechType, Integer>();
		this.ownTechsInProduction = new HashMap<TechType, Integer>();
		this.ownUnits = new HashMap<UnitType, Integer>();
		this.ownUnitsInProduction = new HashMap<UnitType, Integer>();
		
		this.oppUnits = new HashMap<UnitType, Integer>();
		this.observations = new ArrayList<Observation>();
		
		this.refineries = new ArrayList<Unit>();
		this.refineriesInProd = new ArrayList<Unit>();
		this.possibleEnemyBasePositions = new ArrayList<BaseLocation>();
		
		this.bases = new ArrayList<Unit>();
		this.pylons = new ArrayList<Unit>();
		
		this.ownBaseLocations = new ArrayList<BaseLocation>();
		
		ITUBot.getInstance().addListener(this);
		
	}
	
	@Override
	public void execute() {
		this.ownUnits.clear();
		this.ownUnitsInProduction.clear();
		this.ownTechs.clear();
		this.ownTechsInProduction.clear();
		this.ownUpgrades.clear();
		this.ownUpgradesInProduction.clear();
		this.oppUnits.clear();
		this.refineries.clear();
		this.refineriesInProd.clear();
		
		for (Unit unit : Self.getInstance().getUnits()){
			
			if (unit.isBeingConstructed()){
				int i = 0;
				if (this.ownUnitsInProduction.containsKey(unit.getType())){
					i = this.ownUnitsInProduction.get(unit.getType());
				}
				this.ownUnitsInProduction.put(unit.getType(), i+1);
				if (unit.getType().isRefinery()){
					this.refineriesInProd.add(unit);
				}
			} else {
				int i = 0;
				if (this.ownUnits.containsKey(unit.getType())){
					i = this.ownUnits.get(unit.getType());
				}
				this.ownUnits.put(unit.getType(), i+1);
				if (unit.getType().isRefinery()){
					this.refineries.add(unit);
				}
				
			}
		}
		for(Unit unit : Enemy.getInstance().getUnits()){
			if (unit.getPosition().isValid()){
				boolean found = false;
				for (Observation observation : observations){
					if (observation.id == unit.getID()){
						observation.position = new Position(unit.getPosition().getX(), unit.getPosition().getY());
						found = true;
						break;
					}
				}
				if (!found){
					observations.add(new Observation(unit));
					if (enemyBaseLocation == null && unit.getPlayer().isEnemy(Self.getInstance())){
						BotLogger.getInstance().log(this, "Enemy (" + unit.getPlayer().getRace() + " " + unit.getType() + ") base found at " + unit.getPosition());
						if (unit.getType().isBuilding()){
							BotLogger.getInstance().log(this, "Enemy (" + unit.getPlayer().getRace() + ") base found at " + unit.getPosition());
							for(BaseLocation location : possibleEnemyBasePositions){
								if (unit.getDistance(location.getPosition()) < 1000){
									enemyBaseLocation = location;
									break;
								}
							}
						}
					}
				}
			}
		}
		
		for(TechType tech : TechTypes.all){
			ownTechs.put(tech, Self.getInstance().hasResearched(tech) ? 1 : 0);
			ownTechsInProduction.put(tech, Self.getInstance().isResearching(tech) ? 1 : 0);
		}
		for(UpgradeType upgrade : UpgradeTypes.all){
			ownUpgrades.put(upgrade, Self.getInstance().getUpgradeLevel(upgrade));
			ownUpgradesInProduction.put(upgrade, Self.getInstance().getUpgradeLevel(upgrade) + (Self.getInstance().isUpgrading(upgrade) ? 1 : 0));
		}
		
		
		
	}
	
	public int ownTechCountTotal(TechType tech) {
		return ownTechs.get(tech) + ownTechsInProduction.get(tech);
	}
	
	public int ownTechCount(TechType tech) {
		return ownTechs.get(tech);
	}
	
	public int ownUpgradeCountTotal(UpgradeType upgrade) {
		return ownUpgrades.get(upgrade) + ownUpgradesInProduction.get(upgrade);
	}
	
	public int ownUpgradeCount(UpgradeType upgrade) {
		return ownUpgrades.get(upgrade);
	}
		
	public int ownUnitCount(UnitType unitType) {
		int count = 0;
		if (ownUnits.containsKey(unitType)){
			count += ownUnits.get(unitType);
		}
		return count;
	}
	
	public int ownUnitCountTotal(UnitType unitType) {
		int count = 0;
		if (ownUnits.containsKey(unitType)){
			count += ownUnits.get(unitType);
		}
		if (ownUnitsInProduction.containsKey(unitType)){
			count += ownUnitsInProduction.get(unitType);
		}
		return count;
	}
	

	public int ownUnitCountInProd(UnitType unitType) {
		int count = 0;
		if (ownUnitsInProduction.containsKey(unitType)){
			count += ownUnitsInProduction.get(unitType);
		}
		return count;
	}
	
	@Override
	public void visualize() {
		if (enemyBaseLocation == null){
			Match.getInstance().drawTextScreen(12, 22, "Possible base locations: " + possibleEnemyBasePositions.size());
		} else {
			Match.getInstance().drawTextScreen(12, 22, "Enemy base location: " + enemyBaseLocation.getPosition());
		}
		Match.getInstance().drawTextScreen(12, 32, "Own bases: " + ownBaseLocations.size());
		for(Observation observation : observations){
			if (observation.type.isBuilding()){
				int width = observation.type.width();
				int height = observation.type.height();
				Position topLeft = new Position(observation.position.getX() - width/2, observation.position.getY() - height/2);
				Position bottomRight = new Position(observation.position.getX() + width/2, observation.position.getY() + height/2);
				Match.getInstance().drawBoxMap(topLeft, bottomRight, Color.Red);
			} else {
				Match.getInstance().drawCircleMap(observation.position, observation.type.width()/2, Color.Red);
			}
			Match.getInstance().drawTextMap(observation.position, observation.type.toString());
		}
		Match.getInstance().drawTextScreen(12, 62, "Observations: " + observations.size());
		
	}

	@Override
	public void onEnd(boolean arg0) {
	}

	@Override
	public void onFrame() {
		
	}

	@Override
	public void onNukeDetect(Position arg0) {
	}

	@Override
	public void onPlayerDropped(Player arg0) {
	}

	@Override
	public void onPlayerLeft(Player arg0) {
	}

	@Override
	public void onReceiveText(Player arg0, String arg1) {
	}

	@Override
	public void onSaveGame(String arg0) {
	}

	@Override
	public void onSendText(String arg0) {
	}

	@Override
	public void onStart() {
		for (BaseLocation b : BWTA.getBaseLocations()) {
			if (b.isStartLocation()) {
				if (Self.getInstance().getStartLocation().equals(b.getTilePosition())){
					ownMainBaseLocation = b;
				} else {
					possibleEnemyBasePositions.add(b);
				}
			}
		}
	}

	@Override
	public void onUnitComplete(Unit unit) {
		
	}

	@Override
	public void onUnitCreate(Unit unit) {
		if (unit.getType().isResourceDepot()){
			bases.add(unit);
			BaseLocation baseLocation = null;
			for(BaseLocation location : BWTA.getBaseLocations()){
				if (unit.getTilePosition().equals(location.getTilePosition())){
					baseLocation = location;
				}
			}
			if (baseLocation != null){
				this.ownBaseLocations.add(baseLocation);
			}
		}
		if (unit.getType() == UnitType.Protoss_Pylon){
			pylons.add(unit);
		}
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		if (unit.getPlayer().isEnemy(Self.getInstance())){
			Observation toRemove = null;
			for (Observation observation : observations){
				if (observation.id == unit.getID()){
					toRemove = observation;
					break;
				}
			}
			BotLogger.getInstance().log(this, "Before removing " + observations.size());
			observations.remove(toRemove);
			BotLogger.getInstance().log(this, "After removing " + observations.size());
		} else {
			if (unit.getType().isResourceDepot()){
				BaseLocation baseLocation = null;
				for(BaseLocation location : BWTA.getBaseLocations()){
					if (unit.getTilePosition().equals(location.getTilePosition())){
						baseLocation = location;
					}
				}
				if (baseLocation != null){
					this.ownBaseLocations.remove(baseLocation);
				}
				this.bases.remove(unit);
			} else if (unit.getType() == UnitType.Protoss_Pylon){
				pylons.remove(unit);
			} else if (unit.getType().isRefinery()){
				refineries.remove(unit);
			}
		}
		
	}

	@Override
	public void onUnitDiscover(Unit unit) {

	}

	@Override
	public void onUnitEvade(Unit unit) {

	}

	@Override
	public void onUnitHide(Unit arg0) {
	}

	@Override
	public void onUnitMorph(Unit arg0) {
	}

	@Override
	public void onUnitRenegade(Unit arg0) {
	}

	@Override
	public void onUnitShow(Unit unit) {
	}


}