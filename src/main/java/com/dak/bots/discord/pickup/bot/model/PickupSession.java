package com.dak.bots.discord.pickup.bot.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.dak.bots.discord.pickup.exception.PickupBotException;
import com.dak.bots.discord.pickup.util.TableRenderer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Builder(toBuilder=true)
@Getter
@ToString
@Slf4j
public class PickupSession implements Serializable {
	private static final long serialVersionUID = 7830964273186299165L;

	private final String channelId;
	private final Integer teamSize;
	private Set<PickupPlayer> players;
	private final boolean isAutoFilled;
	private final boolean isTeamless;

	@JsonCreator(mode = JsonCreator.Mode.DEFAULT)
	public PickupSession(
			@JsonProperty("channelId") final String channelId, 
			@JsonProperty("teamSize") final Integer teamSize, 
			@JsonProperty("players") final Set<PickupPlayer> players, 
			@JsonProperty("autoFilled") final boolean isAutoFilled,
			@JsonProperty("teamless") final boolean isTeamless) {
		this.channelId = channelId;
		this.teamSize = teamSize;
		this.players = players;
		this.isAutoFilled = isAutoFilled;
		this.isTeamless = isTeamless;
	}

	public PickupSession(
			final String channelId, 
			final Integer teamSize, 
			final PickupPlayer teamOneCaptain, 
			final PickupPlayer teamTwoCaptain) {
		this.channelId = channelId;
		this.teamSize = teamSize;
		this.isAutoFilled = false;
		this.players = new HashSet<>();
		this.players.add(teamOneCaptain);
		this.players.add(teamTwoCaptain);
		this.isTeamless = false;
	}

	public PickupSession(final String channelId, final Integer teamSize, final boolean isTeamless) {
		this.channelId = channelId;
		this.teamSize = teamSize;
		this.isAutoFilled = !isTeamless;
		this.players = new HashSet<>();
		this.isTeamless = isTeamless;
	}

	@JsonIgnore
	public boolean isOversized() {
		return this.players.size() > (this.teamSize * 2);
	}

	@JsonIgnore
	public boolean trimDownToSize() {
		if(this.isOversized()) {
			final Set<PickupPlayer> trimmedPlayers = new HashSet<>();
			trimmedPlayers.addAll(this.players.stream().filter(PickupPlayer::getIsCaptain).collect(Collectors.toList()));
			trimmedPlayers.addAll(this.players.stream().filter(p -> !p.getIsCaptain()).limit((this.teamSize * 2) - 2).collect(Collectors.toList()));
			this.players = trimmedPlayers;
			return true;
		} else {
			return false;
		}
	}

	public boolean removePlayer(final String playerId) {
		return this.players.removeIf(p -> p.getId().equals(playerId));
	}

	public boolean addPlayer(final PickupPlayer player) {
		if(isPlayerInSession(player.getId())) {
			log.trace("player {} already in session...", player);
			return false;
		} else {
			players.add(PickupPlayer.builder().id(player.getId()).tag(player.getTag()).nickname(player.getNickname()).team(PickupTeam.NO_TEAM).isCaptain(false).build());
			return true;
		}
	}

	public boolean assignPlayerToTeam(final String playerId, final PickupTeam team) {
		if(isTeamFull(team)) {
			log.trace("cannot add player to {} - team is full!", team);
			return false;
		}

		final Optional<PickupPlayer> player = getPickupPlayerFromSession(playerId);

		if(player.isPresent()) {
			player.get().setTeam(team);
			return true;
		} else {
			log.trace("player not in session!");
			return false;
		}
	}

	public boolean isTeamFull(final PickupTeam team) {
		return players.stream().filter(a -> team.equals(a.getTeam())).count() == teamSize;
	}

	@JsonIgnore
	public boolean isGameFull() {
		return isTeamFull(PickupTeam.TEAM_ONE) && (isTeamless || isTeamFull(PickupTeam.TEAM_TWO));
	}

	public boolean isPlayerInSession(final String playerId) {
		return getPickupPlayerFromSession(playerId).isPresent();
	}

	@JsonIgnore
	public List<PickupPlayer> getPlayersByTeam(final PickupTeam team){
		return players.stream().filter(a -> team.equals(a.getTeam())).collect(Collectors.toList());
	}

	@JsonIgnore
	private Optional<PickupPlayer> getPickupPlayerFromSession(final String playerId) {
		for(PickupPlayer player : players) {
			if(player.getId().equals(playerId)) {
				return Optional.of(player);
			}
		}

		return Optional.empty();
	}

	@JsonIgnore
	public String prettyPrint() {
		final StringBuilder sb = new StringBuilder();

		if(isTeamless) {
			sb.append("**Team We-don't-need-no-stinkin-teams** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.TEAM_ONE) + "```");
		} else {
			sb.append("**Team One** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.TEAM_ONE) + "```");

			sb.append("\n**Team Two** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.TEAM_TWO) + "```");
			if(getPlayersByTeam(PickupTeam.NO_TEAM).size() > 0) {
				sb.append("\n**Unassigned** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```");
			}
		}

		return sb.toString();
	}

	@JsonIgnore
	public String getPrettyPrintedPlayersByTeam(final PickupTeam team) {
		final TableRenderer teamRenderer = new TableRenderer();
		if(PickupTeam.NO_TEAM.equals(team)) {
			teamRenderer.setHeader("#", "Player Name");
		} else {
			teamRenderer.setHeader("#", "Player Name", "Captain?");
		}
		final List<PickupPlayer> players = getPlayersByTeam(team);
		int count = 0;
		for(PickupPlayer player : players) {
			if(PickupTeam.NO_TEAM.equals(team)) {
				teamRenderer.addRow(++count, player.getNickname());
			} else {
				teamRenderer.addRow(++count, player.getNickname(), player.getIsCaptain() ? "X" : "");
			}
		}

		while(count < teamSize) {
			teamRenderer.addRow(++count, "", "");
		}

		return teamRenderer.build();
	}

	@JsonIgnore
	public Integer getNumberOfPlayersNeeded() {
		if(isTeamless) {
			return teamSize - players.size();
		} else {
			return (teamSize * 2) - players.size();
		}
	}

	@JsonIgnore
	private Optional<PickupPlayer> getCaptain(final PickupTeam team) {
		return players.stream().filter(a -> a.getIsCaptain() && a.getTeam().equals(team)).findFirst();
	}

	@JsonIgnore
	public Optional<PickupPlayer> getNextCaptainToPick() {
		if(isGameFull()) {
			return Optional.empty();
		}

		final long teamOneSize = players.stream().filter(a -> PickupTeam.TEAM_ONE.equals(a.getTeam())).count();
		final long teamTwoSize = players.stream().filter(a -> PickupTeam.TEAM_TWO.equals(a.getTeam())).count();

		if(teamOneSize > teamTwoSize) {
			return getCaptain(PickupTeam.TEAM_TWO);
		} else {
			return getCaptain(PickupTeam.TEAM_ONE);
		}
	}

	public void populateSession() {
		if(this.isAutoFilled) {
			final List<PickupPlayer> shuffledPlayers = players.stream().collect(toShuffledList());
			for(int i = 0; i < teamSize*2; i++) {
				shuffledPlayers.get(i).setTeam(i % 2 == 0 ? PickupTeam.TEAM_ONE : PickupTeam.TEAM_TWO);
			}
		} else if(this.isTeamless) {
			 players.forEach(p -> p.setTeam(PickupTeam.TEAM_ONE));
		} else {
			throw new PickupBotException("game is not auto-fillable!");
		}
	}

	private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(
			Collectors.toCollection(ArrayList::new),
			list -> {
				Collections.shuffle(list);
				return list;
			}
			);

	@SuppressWarnings("unchecked")
	private static <T> Collector<T, ?, List<T>> toShuffledList() {
		return (Collector<T, ?, List<T>>) SHUFFLER;
	}
}
