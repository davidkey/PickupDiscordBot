package com.dak.bots.discord.pickup.bot.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class PickupPlayer implements Serializable {
	private static final long serialVersionUID = 4936044237131172659L;
	
	private final String id;
	private final String tag;
	private final String nickname;
	private final Boolean isCaptain;
	private PickupTeam team;
	
	@JsonCreator(mode = JsonCreator.Mode.DEFAULT)
	public PickupPlayer(
			@JsonProperty("id") final String id, 
			@JsonProperty("tag") final String tag, 
			@JsonProperty("nickname") final String nickname, 
			@JsonProperty("isCaptain") final Boolean isCaptain,
			@JsonProperty("team") final PickupTeam team) {
		this.id = id;
		this.tag = tag;
		this.nickname = nickname;
		this.isCaptain = isCaptain;
		this.team = team;
	}
}
