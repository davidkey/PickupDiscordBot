package com.dak.bots.discord.pickup.model;

import com.dak.bots.discord.pickup.exception.InvalidPickupCommandException;

import lombok.Getter;

@Getter
public enum PickupCommandType {
	HELP("help", 0),
	CAPTAIN("captain", 3), // two captains and a team size!
	AUTO("auto", 1), // team size
	ADD("add", 0),
	PICK("pick", 1),
	STATUS("status", 0),
	CLEAR("clear", 0);
	
	private final String commandText;
	private final Integer numArgs;
	
	private PickupCommandType(final String commandText, final Integer numArgs) {
		this.commandText = commandText;
		this.numArgs = numArgs;
	}
	
	public static PickupCommandType fromString(final String s) {
		for(PickupCommandType fc : PickupCommandType.values()) {
			if(fc.commandText.equalsIgnoreCase(s)) {
				return fc;
			}
		}
		
		throw new InvalidPickupCommandException("Command " + s + " not valid!");
	}
}
