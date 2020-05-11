package com.dak.bots.discord.pickup.bot;

import org.springframework.stereotype.Component;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PickupBot extends ListenerAdapter {

	private final BotService pickupBotService;

	@Override
	public void onMessageReceived(final MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.TEXT)) {
			/**
			 * Ignore messages from other bots
			 */
			if(event.getAuthor().isBot()) {
				return;
			}

			final String text = event.getMessage().getContentRaw().trim();

			if(text != null && text.startsWith("!pickup")) {
				final MessageChannel channel = event.getChannel();

				/**
				 * Validate message; send help if invalid
				 */
				if(!PickupCommandMessage.validateMessage(text)) {
					pickupBotService.sendHelpMsg(channel, true);
					return;
				}

				final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);

				log.trace("pickup message received: {}", pickupMessage);

				/**
				 * Dispatch command
				 */
				pickupMessage.getCommand().execute(event, pickupBotService);
			}
		}
	}
}
