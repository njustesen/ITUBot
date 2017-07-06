package itubot.util;

import java.util.ArrayList;

import bwapi.Player;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import itubot.abstraction.Build;

public class TypeRepository {

	public static ArrayList<UnitType> protossUnits = new ArrayList<UnitType>() {{
		add(UnitType.Protoss_Corsair);
		add(UnitType.Protoss_Dark_Templar);
		add(UnitType.Protoss_Dark_Archon);
		add(UnitType.Protoss_Probe);
		add(UnitType.Protoss_Zealot);
		add(UnitType.Protoss_Dragoon);
		add(UnitType.Protoss_High_Templar);
		add(UnitType.Protoss_Archon);
		add(UnitType.Protoss_Shuttle);
		add(UnitType.Protoss_Scout);
		add(UnitType.Protoss_Arbiter);
		add(UnitType.Protoss_Carrier);
		add(UnitType.Protoss_Interceptor);
		add(UnitType.Protoss_Reaver);
		add(UnitType.Protoss_Observer);
		add(UnitType.Protoss_Scarab);
		add(UnitType.Protoss_Nexus);
		add(UnitType.Protoss_Robotics_Facility);
		add(UnitType.Protoss_Pylon);
		add(UnitType.Protoss_Assimilator);
		add(UnitType.Protoss_Observatory);
		add(UnitType.Protoss_Gateway);
		add(UnitType.Protoss_Photon_Cannon);
		add(UnitType.Protoss_Citadel_of_Adun);
		add(UnitType.Protoss_Cybernetics_Core);
		add(UnitType.Protoss_Templar_Archives);
		add(UnitType.Protoss_Forge);
		add(UnitType.Protoss_Stargate);
		add(UnitType.Protoss_Fleet_Beacon);
		add(UnitType.Protoss_Arbiter_Tribunal);
		add(UnitType.Protoss_Robotics_Support_Bay);
		add(UnitType.Protoss_Shield_Battery);
	}};
	
	public static ArrayList<UnitType> terranUnits = new ArrayList<UnitType>() {{
		add(UnitType.Terran_Marine);
		add(UnitType.Terran_Ghost);
		add(UnitType.Terran_Vulture);
		add(UnitType.Terran_Goliath);
		add(UnitType.Terran_Siege_Tank_Tank_Mode);
		add(UnitType.Terran_SCV);
		add(UnitType.Terran_Wraith);
		add(UnitType.Terran_Science_Vessel);
		add(UnitType.Terran_Dropship);
		add(UnitType.Terran_Battlecruiser);
		add(UnitType.Terran_Vulture_Spider_Mine);
		add(UnitType.Terran_Nuclear_Missile);
		add(UnitType.Terran_Firebat);
		add(UnitType.Terran_Medic);
		add(UnitType.Terran_Valkyrie);
		add(UnitType.Terran_Command_Center);
		add(UnitType.Terran_Comsat_Station);
		add(UnitType.Terran_Nuclear_Silo);
		add(UnitType.Terran_Supply_Depot);
		add(UnitType.Terran_Refinery);
		add(UnitType.Terran_Barracks);
		add(UnitType.Terran_Academy);
		add(UnitType.Terran_Factory);
		add(UnitType.Terran_Starport);
		add(UnitType.Terran_Control_Tower);
		add(UnitType.Terran_Science_Facility);
		add(UnitType.Terran_Covert_Ops);
		add(UnitType.Terran_Physics_Lab);
		add(UnitType.Terran_Machine_Shop);
		add(UnitType.Terran_Engineering_Bay);
		add(UnitType.Terran_Missile_Turret);
		add(UnitType.Terran_Bunker);
		add(UnitType.Terran_Armory);
	}};
	
	public static ArrayList<UnitType> zergUnits = new ArrayList<UnitType>() {{
		add(UnitType.Zerg_Larva);
		add(UnitType.Zerg_Egg);
		add(UnitType.Zerg_Zergling);
		add(UnitType.Zerg_Hydralisk);
		add(UnitType.Zerg_Ultralisk);
		add(UnitType.Zerg_Broodling);
		add(UnitType.Zerg_Drone);
		add(UnitType.Zerg_Overlord);
		add(UnitType.Zerg_Mutalisk);
		add(UnitType.Zerg_Guardian);
		add(UnitType.Zerg_Queen);
		add(UnitType.Zerg_Defiler);
		add(UnitType.Zerg_Scourge);
		add(UnitType.Zerg_Infested_Terran);
		add(UnitType.Zerg_Cocoon);
		add(UnitType.Zerg_Devourer);
		add(UnitType.Zerg_Lurker_Egg);
		add(UnitType.Zerg_Infested_Command_Center);
		add(UnitType.Zerg_Hatchery);
		add(UnitType.Zerg_Lair);
		add(UnitType.Zerg_Hive);
		add(UnitType.Zerg_Nydus_Canal);
		add(UnitType.Zerg_Hydralisk_Den);
		add(UnitType.Zerg_Defiler_Mound);
		add(UnitType.Zerg_Greater_Spire);
		add(UnitType.Zerg_Queens_Nest);
		add(UnitType.Zerg_Evolution_Chamber);
		add(UnitType.Zerg_Ultralisk_Cavern);
		add(UnitType.Zerg_Spire);
		add(UnitType.Zerg_Spawning_Pool);
		add(UnitType.Zerg_Creep_Colony);
		add(UnitType.Zerg_Spore_Colony);
		add(UnitType.Zerg_Sunken_Colony);
		add(UnitType.Zerg_Extractor);
	}};
	
	public static ArrayList<TechType> protossTechs = new ArrayList<TechType>() {{
		add(TechType.Psionic_Storm);
		add(TechType.Hallucination);
		add(TechType.Recall);
		add(TechType.Stasis_Field);
		add(TechType.Archon_Warp);
		add(TechType.Disruption_Web);
		add(TechType.Mind_Control);
		add(TechType.Dark_Archon_Meld);
		add(TechType.Feedback);
		add(TechType.Maelstrom);
	}};
	
	public static ArrayList<TechType> terranTechs = new ArrayList<TechType>() {{
		add(TechType.Stim_Packs);
		add(TechType.Lockdown);
		add(TechType.EMP_Shockwave);
		add(TechType.Spider_Mines);
		add(TechType.Scanner_Sweep);
		add(TechType.Tank_Siege_Mode);
		add(TechType.Defensive_Matrix);
		add(TechType.Irradiate);
		add(TechType.Yamato_Gun);
		add(TechType.Cloaking_Field);
		add(TechType.Personnel_Cloaking);
		add(TechType.Restoration);
		add(TechType.Optical_Flare);
		add(TechType.Nuclear_Strike);
	}};
	
	public static ArrayList<TechType> zergTechs = new ArrayList<TechType>() {{
		add(TechType.Burrowing);
		add(TechType.Infestation);
		add(TechType.Spawn_Broodlings);
		add(TechType.Dark_Swarm);
		add(TechType.Plague);
		add(TechType.Consume);
		add(TechType.Ensnare);
		add(TechType.Parasite);
		add(TechType.Lurker_Aspect);
	}};
	
	public static ArrayList<UpgradeType> protossUpgrades = new ArrayList<UpgradeType>() {{
		add(UpgradeType.Protoss_Ground_Armor);
		add(UpgradeType.Protoss_Air_Armor);
		add(UpgradeType.Protoss_Ground_Weapons);
		add(UpgradeType.Protoss_Air_Weapons);
		add(UpgradeType.Protoss_Plasma_Shields);
		add(UpgradeType.Singularity_Charge);
		add(UpgradeType.Leg_Enhancements);
		add(UpgradeType.Scarab_Damage);
		add(UpgradeType.Reaver_Capacity);
		add(UpgradeType.Gravitic_Drive);
		add(UpgradeType.Sensor_Array);
		add(UpgradeType.Gravitic_Boosters);
		add(UpgradeType.Khaydarin_Amulet);
		add(UpgradeType.Apial_Sensors);
		add(UpgradeType.Gravitic_Thrusters);
		add(UpgradeType.Carrier_Capacity);
		add(UpgradeType.Khaydarin_Core);
		add(UpgradeType.Argus_Jewel);
		add(UpgradeType.Argus_Talisman);
	}};
	
	public static ArrayList<UpgradeType> terranUpgrades = new ArrayList<UpgradeType>() {{
		add(UpgradeType.Terran_Infantry_Armor);
		add(UpgradeType.Terran_Vehicle_Plating);
		add(UpgradeType.Terran_Ship_Plating);
		add(UpgradeType.Terran_Infantry_Weapons);
		add(UpgradeType.Terran_Vehicle_Weapons);
		add(UpgradeType.Terran_Ship_Weapons);
		add(UpgradeType.U_238_Shells);
		add(UpgradeType.Ion_Thrusters);
		add(UpgradeType.Titan_Reactor);
		add(UpgradeType.Ocular_Implants);
		add(UpgradeType.Moebius_Reactor);
		add(UpgradeType.Apollo_Reactor);
		add(UpgradeType.Colossus_Reactor);
		add(UpgradeType.Caduceus_Reactor);
		add(UpgradeType.Charon_Boosters);
	}};
	
	public static ArrayList<UpgradeType> zergUpgrades = new ArrayList<UpgradeType>() {{
		add(UpgradeType.Zerg_Carapace);
		add(UpgradeType.Zerg_Flyer_Carapace);
		add(UpgradeType.Zerg_Melee_Attacks);
		add(UpgradeType.Zerg_Missile_Attacks);
		add(UpgradeType.Zerg_Flyer_Attacks);
		add(UpgradeType.Ventral_Sacs);
		add(UpgradeType.Antennae);
		add(UpgradeType.Pneumatized_Carapace);
		add(UpgradeType.Metabolic_Boost);
		add(UpgradeType.Adrenal_Glands);
		add(UpgradeType.Muscular_Augments);
		add(UpgradeType.Grooved_Spines);
		add(UpgradeType.Gamete_Meiosis);
		add(UpgradeType.Metasynaptic_Node);
		add(UpgradeType.Chitinous_Plating);
		add(UpgradeType.Anabolic_Synthesis);
	}};
	
	public static int getUnitIdForRace(UnitType unitType, Race race){
		if (race == Race.Protoss){
			return protossUnits.indexOf(unitType);
		}
		if (race == Race.Terran){
			return terranUnits.indexOf(unitType);
		}
		if (race == Race.Zerg){
			return zergUnits.indexOf(unitType);
		}
		return -1;
	}

	public static int getTechIdForRace(TechType techType, Race race, boolean globalIdx) {
		if (race == Race.Protoss){
			if (globalIdx)
				return protossTechs.indexOf(techType) + protossUnits.size();
			else
				return protossTechs.indexOf(techType);
		}
		if (race == Race.Terran){
			if (globalIdx)
				return terranTechs.indexOf(techType) + terranUnits.size();
			else
				return terranTechs.indexOf(techType);
		}
		if (race == Race.Zerg){
			if (globalIdx)
				return zergTechs.indexOf(techType) + zergUnits.size();
			else
				return zergTechs.indexOf(techType);
		}
		return -1;
	}
	
	public static int getUpgradeIdForRace(UpgradeType upgradeType, Race race, boolean globalIdx) {
		if (race == Race.Protoss){
			if (globalIdx)
				return protossUpgrades.indexOf(upgradeType) + protossUnits.size() + protossTechs.size();
			else
				return protossUpgrades.indexOf(upgradeType);
		}
		if (race == Race.Terran){
			if (globalIdx)
				return terranUpgrades.indexOf(upgradeType) + terranUnits.size() + terranTechs.size();
			else
				return terranUpgrades.indexOf(upgradeType);
		}
		if (race == Race.Zerg){
			if (globalIdx)
				return zergUpgrades.indexOf(upgradeType) + zergUnits.size() + zergTechs.size();
			else
				return zergUpgrades.indexOf(upgradeType);
		}
		return -1;
	}

	public static Build buildForRace(int id, Race race) {
		if (race == Race.Protoss){
			if (id < protossUnits.size())
				return new Build(protossUnits.get(id));
			if (id < protossUnits.size() + protossTechs.size())
				return new Build(protossTechs.get(protossUnits.size() + id));
			if (id < protossUnits.size() + protossTechs.size() + protossUpgrades.size())
				return new Build(protossUpgrades.get(protossUnits.size() + protossTechs.size() + id));
		}
		if (race == Race.Terran){
			if (id < terranUnits.size())
				return new Build(terranUnits.get(id));
			if (id < terranUnits.size() + terranTechs.size())
				return new Build(terranTechs.get(terranUnits.size() + id));
			if (id < terranUnits.size() + terranTechs.size() + terranUpgrades.size())
				return new Build(terranUpgrades.get(terranUnits.size() + terranTechs.size() + id));
		}
		if (race == Race.Zerg){
			if (id < zergUnits.size())
				return new Build(zergUnits.get(id));
			if (id < zergUnits.size() + zergTechs.size())
				return new Build(zergTechs.get(zergUnits.size() + id));
			if (id < zergUnits.size() + zergTechs.size() + zergUpgrades.size())
				return new Build(zergUpgrades.get(zergUnits.size() + zergTechs.size() + id));
		}
		return null;
	}
	
}
