package itubot.bwapi;

import bwapi.Mirror;

public class BWAPI {

	// SINGLETON PATTERN
	private static Mirror instance = null;
	
	public static Mirror getInstance() {
	   if(instance == null) {
		   instance = new Mirror();
	   }
	   return instance;
	}
	
}
