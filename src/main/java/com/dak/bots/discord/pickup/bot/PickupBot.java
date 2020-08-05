package com.dak.bots.discord.pickup.bot;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@Slf4j
public class PickupBot extends ListenerAdapter {

	private final BotService pickupBotService;
	private final String commandString;
	private final Boolean isCommandStringCaseSensitive;
	private final String botClientId;

	public PickupBot(
			final BotService pickupBotService, 
			@Value("${bot.commandString}") final String commandString, 
			@Value("${bot.commandStringCaseSensitive}") final Boolean isCommandStringCaseSensitive,
			@Value("${bot.clientId}") final String botClientId) {
		this.pickupBotService = pickupBotService;
		this.commandString = commandString;
		this.isCommandStringCaseSensitive = isCommandStringCaseSensitive;
		this.botClientId = botClientId;
	}

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

			/**
			 * If someone mentions the bot, respond accordingly
			 */
			if(wasBotMentioned(event)) {
				log.trace("someone mentioned us ({})", text);
				replyToBotMention(event);
			}

			if(isMessageForBot(text)) {
				final MessageChannel channel = event.getChannel();
				
				/**
				 * Validate message; send help if invalid
				 */
				if(!PickupCommandMessage.validateMessage(text)) {
					log.trace("invalid message sent: {}");
					pickupBotService.sendHelpMsg(channel, true);
					return;
				}

				final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);
				
				/**
				 * Log command
				 */
				log.trace("pickup message received: guildId={}; user={}; channel={}; channelId={}; pickupMessage={}; raw={}", 
						event.getGuild().getId(), event.getAuthor().getAsTag(), channel.getName(), channel.getId(), pickupMessage, text);

				/**
				 * Check permissions
				 */
				if(!pickupBotService.hasPermissions(pickupMessage.getCommand(), event)) {
					log.trace("{} does not have permissions to run command {}", event.getAuthor().getAsTag(), pickupMessage.getCommand());
					pickupBotService.sendPermissionsErrorMsg(event.getChannel());
					return;
				}

				/**
				 * Dispatch command
				 */
				pickupMessage.getCommand().execute(event, pickupBotService);
			}
		}
	}

	private void replyToBotMention(final MessageReceivedEvent event) {
		// note that we're not closing this stream because addFile handles that for us
		final InputStream is = getClass().getClassLoader().getResourceAsStream("images/youTalkinToMe.gif");
		event.getChannel().sendMessage(event.getAuthor().getAsMention()).addFile(is, "youTalkinToMe.gif").queue();
	}

	private Boolean wasBotMentioned(final MessageReceivedEvent event) {
		final List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

		for(final Member member : mentionedMembers) {
			if(member.getId().equals(botClientId)) {
				return true;
			}
		}

		return false;
	}

	private Boolean isMessageForBot(final String input) {
		if(input == null || input.isEmpty()) {
			return false;
		}

		if(isCommandStringCaseSensitive) {
			return input.startsWith(commandString);
		} else {
			return input.toLowerCase().startsWith(commandString.toLowerCase());
		}
	}
}
