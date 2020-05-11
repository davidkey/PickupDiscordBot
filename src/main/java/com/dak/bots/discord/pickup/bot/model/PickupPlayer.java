package com.dak.bots.discord.pickup.bot.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class PickupPlayer {
	private final String id;
	private final String tag;
	private final String nickname;
	private final Boolean isCaptain;
	private PickupTeam team;
}
