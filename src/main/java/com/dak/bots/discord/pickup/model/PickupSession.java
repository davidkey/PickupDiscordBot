package com.dak.bots.discord.pickup.model;

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

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@Slf4j
public class PickupSession {

	private final String channelId;
	private final Integer teamSize;
	private Set<PickupPlayer> players;
	private final Boolean isAutoFilled;
	
	public PickupSession(final String channelId, final Integer teamSize, final PickupPlayer teamOneCaptain, final PickupPlayer teamTwoCaptain) {
		this.channelId = channelId;
		this.teamSize = teamSize;
		this.isAutoFilled = false;
		this.players = new HashSet<>();
		this.players.add(teamOneCaptain);
		this.players.add(teamTwoCaptain);
	}
	
	public PickupSession(final String channelId, final Integer teamSize) {
		this.channelId = channelId;
		this.teamSize = teamSize;
		this.isAutoFilled = true;
		this.players = new HashSet<>();
	}
	
	public Boolean addPlayer(final PickupPlayer player) {
		if(isPlayerInSession(player.getId())) {
			log.trace("player {} already in session...", player);
			return false;
		} else {
			players.add(PickupPlayer.builder().id(player.getId()).tag(player.getTag()).nickname(player.getNickname()).team(PickupTeam.NO_TEAM).isCaptain(false).build());
			return true;
		}
	}
	
	public Boolean assignPlayerToTeam(final String playerId, final PickupTeam team) {
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
	
	public Boolean isTeamFull(final PickupTeam team) {
		return players.stream().filter(a -> team.equals(a.getTeam())).count() == teamSize;
	}
	
	public Boolean isGameFull() {
		return isTeamFull(PickupTeam.TEAM_ONE) && isTeamFull(PickupTeam.TEAM_TWO);
	}
	
	public Boolean isPlayerInSession(final String playerId) {
		return getPickupPlayerFromSession(playerId).isPresent();
	}
	
	public List<PickupPlayer> getPlayersByTeam(final PickupTeam team){
		return players.stream().filter(a -> team.equals(a.getTeam())).collect(Collectors.toList());
	}
	
	private Optional<PickupPlayer> getPickupPlayerFromSession(final String playerId) {
		for(PickupPlayer player : players) {
			if(player.getId().equals(playerId)) {
				return Optional.of(player);
			}
		}
		
		return Optional.empty();
	}
	
	public String prettyPrint() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("**Team One** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.TEAM_ONE) + "```");
		sb.append("\n**Team Two** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.TEAM_TWO) + "```");
		if(getPlayersByTeam(PickupTeam.NO_TEAM).size() > 0) {
			sb.append("\n**Unassigned** ```" + getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```");
		}
		
		return sb.toString();
	}
	
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
	
	public Integer getNumberOfPlayersNeeded() {
		return (teamSize * 2) - players.size();
	}
	
	private Optional<PickupPlayer> getCaptain(final PickupTeam team) {
		return players.stream().filter(a -> a.getIsCaptain() && a.getTeam().equals(team)).findFirst();
	}
	
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
	
	public void populateRosters() {
		if(this.isAutoFilled) {
			final List<PickupPlayer> shuffledPlayers = players.stream().collect(toShuffledList());
			for(int i = 0; i < teamSize*2; i++) {
				shuffledPlayers.get(i).setTeam(i % 2 == 0 ? PickupTeam.TEAM_ONE : PickupTeam.TEAM_TWO);
			}
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
