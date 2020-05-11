package com.dak.bots.discord.pickup.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
	
	public BotService() {
		this.sessions = new HashMap<>();
	}
	
	public Boolean hasExistingSession(final String guildId) {
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
	
	public Boolean hasPermissions(final PickupCommand commandType, final MessageReceivedEvent event) {
		final String userId = event.getAuthor().getId();
		if(PickupCommand.CAPTAIN.equals(commandType) || PickupCommand.CLEAR.equals(commandType) || PickupCommand.AUTO.equals(commandType)) {
			final List<Role> captainRoles = event.getGuild().getRoles().stream()
					.filter(r -> r.getName().equalsIgnoreCase("Pickup Bot Admin Role"))
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
				"```!pickup captain @captain_one @captain_two [team size]```" +
				"\nStart a new pickup game with randomly selected teams:" +
				"```!pickup auto [team size]```" +
				"\nAdd yourself as a player to an ongoing pickup session:" +
				"```!pickup add```" + 
				"\nAs a captain, pick a player from the queue (only if queue is full):" +
				"```!pickup pick @user```" +
				"\nView session status:" +
				"```!pickup status```" +
				"\nClear existing session:" +
				"```!pickup clear```"
				)
		.queue();
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
		channel.sendMessage("Role ``Pickup Bot Admin Role`` is missing from this server. Please ask a server admin to add it.").queue();
	}
}
