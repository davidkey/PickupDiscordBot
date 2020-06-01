package com.dak.bots.discord.pickup.bot.commands.impl;

import java.util.Optional;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.bot.model.PickupPlayer;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.bot.model.PickupTeam;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Slf4j
public class PickCommand implements PickupCommandExecutor {

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");

		// make sure there's an outstanding session
		if(service.hasExistingSession(guildId)) {
			final PickupSession session = service.getSession(guildId).get();

			final Optional<PickupPlayer> captain = session.getNextCaptainToPick();
			if(captain.isPresent() && captain.get().getId().equals(event.getAuthor().getId())){
				final String text = event.getMessage().getContentRaw().trim();
				final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);
				final String[] args = pickupMessage.getArgs();

				//final String playerTagToAdd = args[0];
				log.debug("playerIdToAdd: {}", args[0]);
				final String playerIdToAdd = args[0].replaceAll("[^0-9]", "");
				final User playerToAdd = event.getGuild().getMemberById(playerIdToAdd).getUser(); // is this right?
				session.assignPlayerToTeam(playerToAdd.getId(), captain.get().getTeam());

				if(session.isGameFull()) {
					// game is full - show message
					service.sendGameReadyMsg(event.getChannel(), session);
					service.removeSession(guildId);
				} else {
					final Optional<PickupPlayer> nextCaptain = session.getNextCaptainToPick();
					final User nextCaptainUser = event.getGuild().getMemberById(nextCaptain.get().getId()).getUser();
					event.getChannel().sendMessage(
							playerToAdd.getAsMention() + " added to " + captain.get().getTeam() + ". "
									+ nextCaptainUser.getAsMention()
									+ ", it's your turn. Please ``" + service.getCommandString() + " pick`` a player. \n" 
									+ session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM)).queue();
				}

			} else {
				// it's not your turn!
				event.getChannel().sendMessage(event.getAuthor().getAsMention() + " - it's not your turn to pick!");
				return;
			}
		} else {
			service.sendNoSessionMessage(event.getChannel());
			return;
		}
	}

}
