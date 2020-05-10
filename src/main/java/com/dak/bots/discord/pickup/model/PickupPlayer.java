package com.dak.bots.discord.pickup.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class PickupPlayer {

	private final String id;
	private final String nickname;
	private final Boolean isCaptain;
	private PickupTeam team;

}
