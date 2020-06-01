package com.dak.bots.discord.pickup.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Service;

import com.dak.bots.discord.pickup.bot.commands.PickupCommand;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.exception.PickupBotException;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Service
public class BotService {
	
	private final Map<String, PickupSession> sessions;
	private final String adminRoleName;
	private final String commandString;
	private final GitProperties gitProperties;
	private final String baseUrl;
	
	public BotService(
			@Value("${bot.adminRole}") final String adminRoleName, 
			@Value("${bot.commandString}") final String commandString,
			final GitProperties gitProperties,
			@Value("${github.baseUrl}") final String baseUrl) {
		this.sessions = new HashMap<>();
		this.adminRoleName = adminRoleName;
		this.commandString = commandString;
		this.gitProperties = gitProperties;
		this.baseUrl = baseUrl;
	}
	
	public String getScmUrl() {
		return this.baseUrl + this.getGitProperties().getCommitId();
	}
	
	public GitProperties getGitProperties() {
		return this.gitProperties;
	}
	
	public String getCommandString() {
		return this.commandString;
	}
	
	public boolean hasExistingSession(final String guildId) {
		return sessions.containsKey(guildId);
	}
	
	public Optional<PickupSession> getSession(final String guildId) {
		if(sessions.containsKey(guildId)) {
			return Optional.of(sessions.get(guildId));
		} else {
			return Optional.empty();
		}
	}
	
	public void addSession(final String guildId, final PickupSession session) {
		sessions.put(guildId, session);
	}
	
	public void removeSession(final String guildId) {
		sessions.remove(guildId);
	}
	
	public boolean hasPermissions(final PickupCommand commandType, final MessageReceivedEvent event) {
		final String userId = event.getAuthor().getId();
		if(PickupCommand.CAPTAIN.equals(commandType) 
				|| PickupCommand.CLEAR.equals(commandType) 
				|| PickupCommand.AUTO.equals(commandType)
				| PickupCommand.RESIZE.equals(commandType)) {
			final List<Role> captainRoles = event.getGuild().getRoles().stream()
					.filter(r -> adminRoleName.equalsIgnoreCase(r.getName()))
					.collect(Collectors.toList());
			if(captainRoles.isEmpty()) {
				sendRolesMissingMessage(event.getChannel());
				throw new PickupBotException("required roles missing from server!");
			}

			final List<Member> membersWithCaptainRole = event.getGuild().getMembersWithRoles(captainRoles);

			for(final Member member : membersWithCaptainRole) {
				if(member.getId().equals(userId)) {
					return true;
				}
			}

			return false;
		}

		return true; // should this default to true or false? hmm...
	}

	public void sendHelpMsg(final MessageChannel channel, final Boolean hasError) {
		channel.sendMessage((hasError ? "Invalid Pickup Bot format! \n\n" : "") + 
				"Start a new pickup session with captains:" +
				"```" + commandString + " captain @captain_one @captain_two [team size]```" +
				"\nStart a new pickup game with randomly selected teams:" +
				"```" + commandString + " auto [team size]```" +
				"\nAdd yourself as a player to an ongoing pickup session:" +
				"```" + commandString + " add```" + 
				"\nAs a captain, pick a player from the queue (only if queue is full):" +
				"```" + commandString + " pick @user```" +
				"\nView session status:" +
				"```" + commandString + " status```" +
				"\nClear existing session:" +
				"```" + commandString + " clear```" +
				"\nResize existing session:" +
				"```" + commandString + " resize [team size]```" + 
				"\nRemove me from session:" +
				"```" + commandString + " remove```"
				)
		.queue();
	}
	
	public void sendGameResizedMsg(final MessageChannel channel, final PickupSession session) {
		channel.sendMessage("The game has been resized! \n\n" + session.prettyPrint()).queue();
	}

	public void sendPermissionsErrorMsg(final MessageChannel channel) {
		channel.sendMessage("User does not have permissions to run that command!").queue();
	}

	public void sendGameReadyMsg(final MessageChannel channel, final PickupSession session) {
		channel.sendMessage("The game is ready to begin! \n\n" + session.prettyPrint()).queue();
	}

	public void sendNoSessionMessage(final MessageChannel channel) {
		channel.sendMessage("No pickup sessions currently available. Please ask a mod to start a session.").queue();
	}

	public void sendRolesMissingMessage(final MessageChannel channel) {
		channel.sendMessage("Role ``" + adminRoleName + "`` is missing from this server. Please ask a server admin to add it.").queue();
	}
}
