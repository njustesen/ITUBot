package itubot.bwapi;

import bwapi.Player;

public class Enemy {

	public static Player getInstance() {
		for(Player player : Match.getInstance().getPlayers()){
			if (player.isEnemy(Self.getInstance())){
				return player;
			}
		}
		return null;
	}
	
}
