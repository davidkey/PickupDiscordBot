package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.model.PickupPlayer;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.bot.model.PickupTeam;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AddCommand implements PickupCommandExecutor {

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
		if(service.hasExistingSession(guildId)) {
			final PickupSession session = service.getSession(guildId).get();

			if(session.isPlayerInSession(event.getAuthor().getId())) {
				event.getChannel().sendMessage(event.getAuthor().getAsMention() + " - you're already queued!").queue();
				return;
			}

			final User user = event.getAuthor(); 
			final Member userAsMember = event.getGuild().getMemberById(user.getId());

			session.addPlayer(PickupPlayer.builder()
					.id(user.getId())
					.tag(user.getAsTag())
					.nickname(userAsMember.getNickname() != null ? userAsMember.getNickname() : user.getName())
					.build());

			event.getMessage().addReaction("👍").queue();

			if(session.getNumberOfPlayersNeeded() == 0) {
				event.getChannel().sendMessage(user.getAsMention() + " added to session.");

				if(session.isAutoFilled()) {
					// autofill rosters and display
					session.populateSession();
					service.sendGameReadyMsg(event.getChannel(), session);
					service.removeSession(guildId);
				} else {
					// session is ready for captains to pick
					event.getChannel().sendMessage("Enough players are queued for captains to pick players. " 
							+ session.getNextCaptainToPick().get().getTag() + ", please ``!pickup pick`` a player. \n```" 
							+ session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```").queue();
				}
			} else {
				event.getChannel().sendMessage(user.getAsMention() + " added to session. Players in queue: \n" + 
						"```" + session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```").queue();
			}


		} else {
			service.sendNoSessionMessage(event.getChannel());
			return;
		}
	}

}
