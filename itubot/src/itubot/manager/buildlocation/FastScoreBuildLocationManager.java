package itubot.manager.buildlocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.BWEventListener;
import bwapi.Color;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import itubot.abstraction.Observation;
import itubot.bot.ITUBot;
import itubot.bwapi.Match;
import itubot.bwapi.Self;
import itubot.exception.NoBaseLocationsLeftException;
import itubot.exception.NoWorkersException;
import itubot.extension.BwapiHelper;
import itubot.log.BotLogger;
import itubot.manager.IManager;
import itubot.manager.information.InformationManager;

public class FastScoreBuildLocationManager implements IBuildLocationManager {

	private static int[][] psiPatch = new int[][]{
		{0,0,0,0,1,1,1,1,1,1,0,0,0,0},
		{0,0,1,1,1,1,1,1,1,1,1,1,0,0},
		{0,1,1,1,1,1,1,1,1,1,1,1,1,0},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,0,0,1,1,1,1,1,1},
		{1,1,1,1,1,1,0,0,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{0,1,1,1,1,1,1,1,1,1,1,1,1,0},
		{0,0,1,1,1,1,1,1,1,1,1,1,0,0},
		{0,0,0,0,1,1,1,1,1,1,0,0,0,0}
	};

	public int[][] tiles;
	public int[][] psi;
	public int[][] mineralsInRange;
	public int[][] cannonsInRange;
	public int[][] nexusDistance;
	public int[][] blocked;
	
	private int range;
	
	public FastScoreBuildLocationManager(){
	}
	
	private void addPsi(Unit unit) {
		for (int y = 0; y < psiPatch.length; y++){
			for (int x = 0; x < psiPatch[0].length; x++){
				if (psiPatch[y][x] == 1){
					int xx = unit.getTilePosition().getX() - 6 + x;
					int yy = unit.getTilePosition().getY() - 4 + y;
					if (xx >= 0 && xx < Match.getInstance().mapWidth() && yy >= 0 && yy < Match.getInstance().mapHeight()){
						psi[xx][yy] += 1;
					}
				}
			}
		}
	}
	
	private void removePsi(Unit unit) {
		for (int y = 0; y < psiPatch.length; y++){
			for (int x = 0; x < psiPatch[0].length; x++){
				if (psiPatch[y][x] == 1){
					int xx = unit.getTilePosition().getX() - 6 + x;
					int yy = unit.getTilePosition().getY() - 4 + y;
					if (xx >= 0 && xx < Match.getInstance().mapWidth() && yy >= 0 && yy < Match.getInstance().mapHeight()){
						psi[xx][yy] -= 1;
					}
				}
			}
		}
	}
	
	private void addBase(BaseLocation base) {
		BotLogger.getInstance().log(this, "Adding base at " + base);
		Region region = BWTA.getRegion(base.getPosition());
		BotLogger.getInstance().log(this, "Region " + region.getCenter());
		addRegion(region);
		if (base.isStartLocation()){
			for(Chokepoint chokePoint : region.getChokepoints()){
				if (chokePoint.getRegions().first.equals(region)){
					addRegion(chokePoint.getRegions().second);
				} else {
					addRegion(chokePoint.getRegions().first);
				}
			}
			for (Unit mineral : base.getMinerals()){
				addMineralField(mineral.getTilePosition());
			}
		}
		
		for (int r = 0; r < 25; r++){
			for (int x = -r; x <= r; x++){
				for (int y = -r; y <= r; y++){
					if (y != -r && y != r && x != -r && x != r){
						continue;
					}
					TilePosition tile = new TilePosition(base.getPoint().toTilePosition().getX() + x, base.getPoint().toTilePosition().getY() + y);
					Region tileRegion = BWTA.getRegion(tile);
					if (tileRegion != null && region.equals(tileRegion) && Match.getInstance().isBuildable(tile.getX(), tile.getY(), true)){
						nexusDistance[tile.getX()][tile.getY()] = Math.min(nexusDistance[tile.getX()][tile.getY()], r);
					}
				}
			}
		}
		
	}
	
	private void addRegion(Region region) {
		
		for (int x = 0; x < Match.getInstance().mapWidth(); x++){
			for (int y = 0; y < Match.getInstance().mapHeight(); y++){
				Region tileRegion = BWTA.getRegion(new TilePosition(x, y));
				if (tileRegion != null && region.equals(tileRegion) && Match.getInstance().isBuildable(x, y, true)){
					if (tiles[x][y] == -1){
						tiles[x][y] = 0;
					}
				}
			}
		}
		
		for (BaseLocation base : region.getBaseLocations()){
			
			for(Chokepoint chokepoint : BWTA.getRegion(base.getTilePosition()).getChokepoints()){
				Position natural = chokepoint.getCenter();
				fillShortestPath(base.getTilePosition(), natural.toTilePosition(), true);
			}
			for(Unit unit : base.getMinerals()){
				if (unit.exists()){
					fillShortestPaths(base, unit, false);
				}
			}
			for(Unit unit : base.getGeysers()){
				if (unit.exists()){
					fillShortestPaths(base, unit, false);
				}
			}
		}
	}

	private void fillShortestPath(TilePosition a, TilePosition b, boolean manhattan) {
		fillPath(BWTA.getShortestPath(a, b), manhattan);
	}

	private void fillShortestPaths(BaseLocation base, Unit unit, boolean manhattan) {
		double shortestDistance = Integer.MAX_VALUE;
		List<List<TilePosition>> shortestPaths = new ArrayList<List<TilePosition>>();
		List<TilePosition> shortestPath = null;
		for (int x = base.getTilePosition().getX(); x < base.getTilePosition().getX() + UnitType.Protoss_Nexus.tileWidth(); x++){
			for (int y = base.getTilePosition().getY(); y < base.getTilePosition().getY() + UnitType.Protoss_Nexus.tileHeight(); y++){
				TilePosition a = new TilePosition(x, y);
				if (!a.isValid())
					continue;
				for (int xx = unit.getTilePosition().getX(); xx < unit.getTilePosition().getX() + unit.getType().tileWidth(); xx++){
					for (int yy = unit.getTilePosition().getY(); yy < unit.getTilePosition().getY() + unit.getType().tileHeight(); yy++){
						TilePosition b = new TilePosition(xx, yy);
						if (!b.isValid())
							continue;
						if (a.getDistance(b) < shortestDistance){
							shortestDistance = a.getDistance(b);
							shortestPath = BWTA.getShortestPath(a, b);
						}
					}
				}
				if (shortestPath != null){
					shortestPaths.add(shortestPath);
					shortestPath = null;
					shortestDistance = Integer.MAX_VALUE;
				}
			}
		}
		for (List<TilePosition> path : shortestPaths){
			fillPath(path, manhattan);
		}
	}

	private void fillPath(List<TilePosition> path, boolean manhattan) {
		TilePosition lastPosition = null;
		for(TilePosition tile : path){
			tiles[tile.getX()][tile.getY()] = 2;
			if (manhattan && lastPosition != null && isDiagonal(lastPosition, tile)){
				TilePosition position = getManhattanTile(lastPosition, tile);
				tiles[position.getX()][position.getY()] = 2;
			}
			lastPosition = tile;
		}
	}

	private TilePosition getManhattanTile(TilePosition a, TilePosition b) {
		TilePosition c = new TilePosition(a.getX(), b.getY());
		//TilePosition d = new TilePosition(b.getX(), a.getY());
		return c;
	}

	private boolean isDiagonal(TilePosition a, TilePosition b) {
		return (a.getX() != b.getX() && a.getY() != b.getY());
	}

	private void removeBase(BaseLocation base) {
		Region region = BWTA.getRegion(base.getPosition());
		for (int x = 0; x < Match.getInstance().mapWidth(); x++){
			for (int y = 0; y < Match.getInstance().mapHeight(); y++){
				Region tileRegion = BWTA.getRegion(new TilePosition(x, y));
				if (tileRegion != null && tileRegion.equals(region)){
					tiles[x][y] = -1;
				}
			}
		}
	}

	private void fill(Unit unit) {
		for (int x = unit.getTilePosition().getX(); x < unit.getTilePosition().getX() + unit.getType().tileWidth(); x++){
			for (int y = unit.getTilePosition().getY(); y < unit.getTilePosition().getY() + unit.getType().tileHeight(); y++){
				if (x >= 0 && x < Match.getInstance().mapWidth() && y >= 0 && y < Match.getInstance().mapHeight()){
					tiles[x][y] = 1;
				}
			}
		}
	}
	
	private void clear(Unit unit) {
		for (BaseLocation base : BWTA.getBaseLocations()){
			if (unit.getTilePosition().equals(base.getTilePosition())){
				removeBase(base);
				return;
			}
		}
		for (int x = unit.getTilePosition().getX(); x < unit.getTilePosition().getX() + unit.getType().tileWidth(); x++){
			for (int y = unit.getTilePosition().getY(); y < unit.getTilePosition().getY() + unit.getType().tileHeight(); y++){
				if (x >= 0 && x < Match.getInstance().mapWidth() && y >= 0 && y < Match.getInstance().mapHeight()){
					tiles[x][y] = 0;
				}
			}
		}
	}
	
	public boolean isFree(TilePosition position, UnitType buildingType) {
		if (buildingType.isRefinery() || buildingType.isResourceDepot())
			return true;
		for (int x = position.getX(); x < position.getX() + buildingType.tileWidth(); x++){
			for (int y = position.getY(); y < position.getY() + buildingType.tileHeight(); y++){
				if (x >= Match.getInstance().mapWidth() || x < 0 || y >= Match.getInstance().mapHeight() || y < 0){
					//BotLogger.getInstance().log(this, position + " out of map.");
					return false;
				}
				if (buildingType.requiresPsi() && psi[x][y] <= 0){
					//BotLogger.getInstance().log(this, position + " without psi.");
					return false;
				}
				if (tiles[x][y] != 0 || blocked[x][y] != 0){
					//BotLogger.getInstance().log(this, position + " not free.");
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public void execute() {
		blocked = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		for (Unit unit : Self.getInstance().getUnits()){
			if (!unit.getType().isBuilding() && !unit.getType().isWorker()){
				blocked[unit.getTilePosition().getX()][unit.getTilePosition().getY()] = 1;
			}
		}
	}
	
	@Override
	public void visualize() {
		for (int x = 0; x < tiles.length; x++){
			for (int y = 0; y < tiles[0].length; y++){
				if (tiles[x][y] == 0){
					if (blocked[x][y] > 0){
						Match.getInstance().drawBoxMap((x*32)+1, (y*32)+1, ((x+1)*32)-2, ((y+1)*32)-2, Color.Orange);
					} else if (psi[x][y] > 0){
						Match.getInstance().drawBoxMap((x*32)+1, (y*32)+1, ((x+1)*32)-2, ((y+1)*32)-2, Color.Teal);
					} else {
						Match.getInstance().drawBoxMap((x*32)+1, (y*32)+1, ((x+1)*32)-2, ((y+1)*32)-2, Color.Green);
					}
					Match.getInstance().drawTextMap(32*x+2, 32*y, "M:"+mineralsInRange[x][y]);
					Match.getInstance().drawTextMap(32*x+2, 32*y+10, "C:"+cannonsInRange[x][y]);
					Match.getInstance().drawTextMap(32*x+2, 32*y+20, "N:"+nexusDistance[x][y]);
				} else if (tiles[x][y] == 1){
					Match.getInstance().drawBoxMap((x*32)+1, (y*32)+1, ((x+1)*32)-2, ((y+1)*32)-2, Color.Red);
				} else if (tiles[x][y] == 2){
					Match.getInstance().drawBoxMap((x*32)+1, (y*32)+1, ((x+1)*32)-2, ((y+1)*32)-2, Color.Purple);
				}
			}
		}
	}
	
	public TilePosition getLocation(UnitType buildingType) throws NoWorkersException, NoBaseLocationsLeftException{
		
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
		
		// Expansion
		if (buildingType.isResourceDepot()){
			BaseLocation best = null;
			double bestScore = Integer.MIN_VALUE;
			for(BaseLocation location : BWTA.getBaseLocations()){
				if (ITUBot.getInstance().informationManager.getOwnBaseLocations().contains(location)) {
					//BotLogger.getInstance().log(this, location + " is our base already");
				} else if (ITUBot.getInstance().informationManager.getPossibleEnemyBasePositions().contains(location)){
					//BotLogger.getInstance().log(this, location + " might be taken by the enemy");
				} else if (location.isIsland()){
					// TODO: Use dropships to expand to islands
				} else {
					double distanceToHome = Self.getInstance().getStartLocation().toPosition().getDistance(location.getPosition());
					double distanceToEnemy = 0;
					for (Observation observation : ITUBot.getInstance().informationManager.getObservations()){
						distanceToEnemy += observation.position.getDistance(location.getPosition());
					}
					distanceToEnemy = distanceToEnemy / ITUBot.getInstance().informationManager.getObservations().size();
					//BotLogger.getInstance().log(this, "distanceToEnemy: " + distanceToEnemy);
					double score = distanceToEnemy - distanceToHome;
					//BotLogger.getInstance().log(this, "distanceToHome: " + distanceToHome);
					//BotLogger.getInstance().log(this, "score: " + score);
					if (score > bestScore){
						//BotLogger.getInstance().log(this, "Best score " + score);
						bestScore = score;
						best = location;
					}
				}
			}
			if (best == null){
				throw new NoBaseLocationsLeftException();
			} else {
				//BotLogger.getInstance().log(this, "Returning best location " + best.getTilePosition());
				return best.getTilePosition();
			}
		}

		// Other
		TilePosition best = null;
		double bestScore = Integer.MIN_VALUE;
		for (int x = 0; x < tiles.length; x++){
			for (int y = 0; y < tiles[0].length; y++){
				TilePosition position = new TilePosition(x, y);
				if (isFree(position, buildingType)) {
					double score = score(position, buildingType);
					if (score > bestScore){
						bestScore = score;
						best = position;
					}
				}
			}
		}
		
		if (best != null){
			//BotLogger.getInstance().log(this, best + " found with a score: " + bestScore);
			return best;
		}
		
		BotLogger.getInstance().log(this, "Position not found!");
		Match.getInstance().printf("Unable to find suitable build position for "+buildingType.toString());
				
		return null;
		
	}

	private double score(TilePosition tilePosition, UnitType buildingType) {
		double score = 0;
		if (buildingType == UnitType.Protoss_Pylon){
			int nexusDis = nexusDistance[tilePosition.getX()][tilePosition.getY()];
			int psis = psi[tilePosition.getX()][tilePosition.getY()];
			if (nexusDis < 5){
				score = -nexusDis*1000;
			} else if (psis == 0){
				score = -nexusDis;
			} else {
				score = -psis*1000;
			}
		} else if (buildingType == UnitType.Protoss_Photon_Cannon){
			int minerals = mineralsInRange[tilePosition.getX()][tilePosition.getY()];
			int cannons = cannonsInRange[tilePosition.getX()][tilePosition.getY()];
			score = minerals / (cannons+1);
		} else {
			int nexusDis = nexusDistance[tilePosition.getX()][tilePosition.getY()];
			int minerals = mineralsInRange[tilePosition.getX()][tilePosition.getY()];
			if (nexusDis < 10){
				score = -(nexusDis+minerals)*1000;
			} else {
				score = nexusDis-minerals;
			}
		}
		return score;
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
		tiles = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		psi = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		mineralsInRange = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		cannonsInRange = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		blocked = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		range = UnitType.Protoss_Photon_Cannon.airWeapon().maxRange() / 32;
		nexusDistance = new int[Match.getInstance().mapWidth()][Match.getInstance().mapHeight()];
		
		for (int x = 0; x < Match.getInstance().mapWidth(); x++){
			for (int y = 0; y < Match.getInstance().mapHeight(); y++){
				tiles[x][y] = -1;
				psi[x][y] = 0;
				blocked[x][y] = 0;
				nexusDistance[x][y] = 100;
			}
		}
		addBase(ITUBot.getInstance().informationManager.getOwnMainBaseLocation());
	}

	private void addMineralField(TilePosition position) {
		for (int x = position.getX() - range; x < position.getX() + range; x++){
			if (x < 0 || x >= Match.getInstance().mapWidth()){
				continue;
			}
			for (int y = position.getY() - range; y < position.getY() + range; y++){
				if (y < 0 || y >= Match.getInstance().mapHeight()){
					continue;
				}
				mineralsInRange[x][y] += 1;
			}
		}
	}
	
	private void removeMineralField(TilePosition position) {
		for (int x = position.getX() - range; x < position.getX() + range; x++){
			if (x < 0 || x >= Match.getInstance().mapWidth()){
				continue;
			}
			for (int y = position.getY() - range; y < position.getY() + range; y++){
				if (y < 0 || y >= Match.getInstance().mapHeight()){
					continue;
				}
				mineralsInRange[x][y] -= 1;
			}
		}
	}
	
	private void addCannon(TilePosition position) {
		for (int x = position.getX() - range; x < position.getX() + range; x++){
			if (x < 0 || x >= Match.getInstance().mapWidth()){
				continue;
			}
			for (int y = position.getY() - range; y < position.getY() + range; y++){
				if (y < 0 || y >= Match.getInstance().mapHeight()){
					continue;
				}
				cannonsInRange[x][y] += 1;
			}
		}
	}

	private void removeCannon(TilePosition position) {
		for (int x = position.getX() - range; x < position.getX() + range; x++){
			if (x < 0 || x >= Match.getInstance().mapWidth()){
				continue;
			}
			for (int y = position.getY() - range; y < position.getY() + range; y++){
				if (y < 0 || y >= Match.getInstance().mapHeight()){
					continue;
				}
				cannonsInRange[x][y] -= 1;
			}
		}
	}
	
	@Override
	public void onUnitComplete(Unit unit) {
		if (unit.getType() == UnitType.Protoss_Pylon){
			addPsi(unit);
		}
	}

	@Override
	public void onUnitCreate(Unit unit) {
		if (unit.getType().isBuilding() && unit.getPlayer().getID() == Self.getInstance().getID()){
			fill(unit);
			if (unit.getType().isResourceDepot()){
				BotLogger.getInstance().log(this, "Nexus build");
				BotLogger.getInstance().log(this, ITUBot.getInstance().informationManager.getOwnBaseLocations().size() + " base locations known");
				for(BaseLocation base : ITUBot.getInstance().informationManager.getOwnBaseLocations()){
					if (unit.getTilePosition().equals(base.getTilePosition())){
						BotLogger.getInstance().log(this, "Base location " + base.getTilePosition());
						addBase(base);
						return;
					}
				}
				BotLogger.getInstance().log(this, "No base location found");				
			} else if (unit.getType() == UnitType.Protoss_Photon_Cannon){
				addCannon(unit.getTilePosition());
			}
		}
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isBuilding() && unit.getPlayer().getID() == Self.getInstance().getID()){
			clear(unit);
			if (unit.getType() == UnitType.Protoss_Pylon && unit.isCompleted()){
				removePsi(unit);
			}
			if (unit.getType() == UnitType.Protoss_Photon_Cannon){
				removeCannon(unit.getTilePosition());
			}
			if (unit.getType().isMineralField()){
				removeMineralField(unit.getTilePosition());
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
