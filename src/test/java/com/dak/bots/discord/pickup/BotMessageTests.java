package com.dak.bots.discord.pickup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;

import com.dak.bots.discord.pickup.bot.PickupBot;
import com.dak.bots.discord.pickup.bot.commands.PickupCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.HelpCommand;
import com.dak.bots.discord.pickup.mocks.MockSerializationService;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import net.dv8tion.jda.api.entities.Role;

public class BotMessageTests {

	private final BotService pickupBotService;
	private final PickupBot pickupBot;
	
	private final MessageReceivedEvent eventMock;
	private final MessageChannel channelMock;
	private final User userMock;
	private final Message messageMock;
	private final Guild guildMock;
	private final MessageAction messageActionMock;
	private final HelpCommand helpCommandMock;
	private final Role adminRoleMock;
	
	public BotMessageTests() throws Exception {
		this.pickupBotService = spy(new BotService("ADMIN_ROLE", "!pickup", null, "https://", new MockSerializationService()));
		this.pickupBot = spy(new PickupBot(pickupBotService, "!pickup", false, "12345"));
		
		this.eventMock = mock(MessageReceivedEvent.class);
		this.channelMock = mock(MessageChannel.class);
		this.userMock = mock(User.class);
		this.messageMock = mock(Message.class);
		this.guildMock = mock(Guild.class);
		this.messageActionMock = mock(MessageAction.class);
		this.helpCommandMock = mock(HelpCommand.class);
		this.adminRoleMock = mock(Role.class);
		
		when(channelMock.sendMessage(any(CharSequence.class))).thenReturn(messageActionMock);
		doNothing().when(messageActionMock).queue();
		
		
		when(eventMock.isFromType(any(ChannelType.class))).thenReturn(true);
		when(eventMock.getAuthor()).thenReturn(userMock);
		when(eventMock.getChannel()).thenReturn(channelMock);
		when(userMock.isBot()).thenReturn(false);
		
		when(eventMock.getMessage()).thenReturn(messageMock);
		
		when(eventMock.getGuild()).thenReturn(guildMock);
		when(guildMock.getId()).thenReturn("23456");
		
		
		when(channelMock.getName()).thenReturn("Test-Channel-Name");
		when(channelMock.getId()).thenReturn("34567");
		
		when(userMock.getAsTag()).thenReturn("testuser#1234");
		
		when(adminRoleMock.getName()).thenReturn("ADMIN_ROLE");

		PowerMockito.whenNew(HelpCommand.class).withAnyArguments().thenReturn(helpCommandMock);
	}

	@Test
	public void verifyHelpMessageSent() throws Exception {
		when(messageMock.getContentRaw()).thenReturn("!pickup help");
		pickupBot.onMessageReceived(eventMock);
		
		verify(pickupBotService, times(1)).sendHelpMsg(any(), ArgumentMatchers.eq(false));
	}
	
	@Test
	public void verifyErrorMessageSent() throws Exception {
		when(messageMock.getContentRaw()).thenReturn("!pickup someRandomTypo");
		pickupBot.onMessageReceived(eventMock);
		
		verify(pickupBotService, times(1)).sendHelpMsg(any(), ArgumentMatchers.eq(true));
	}

	@Test
	public void verifyPermissionsCheckFail() throws Exception {
		when(messageMock.getContentRaw()).thenReturn("!pickup auto 2");
		doReturn(false).when(pickupBotService).hasPermissions(any(), any());
		pickupBot.onMessageReceived(eventMock);
		
		verify(pickupBotService, times(1)).hasPermissions(any(), any());
		verify(pickupBotService, times(1)).sendPermissionsErrorMsg(any());
	}
	
	@Test
	public void verifyPermissionsCheckSuccess() throws Exception {
		when(messageMock.getContentRaw()).thenReturn("!pickup auto 2");
		doReturn(true).when(pickupBotService).hasPermissions(any(), any());
		pickupBot.onMessageReceived(eventMock);
		
		verify(pickupBotService, times(1)).hasPermissions(any(), any());
		verify(pickupBotService, times(0)).sendPermissionsErrorMsg(any());
	}
}
