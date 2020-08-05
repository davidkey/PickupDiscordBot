package com.dak.bots.discord.pickup.mocks;

import java.util.Collections;
import java.util.Map;

import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.exception.PickupBotException;
import com.dak.bots.discord.pickup.service.SerializationService;

public class MockSerializationService extends SerializationService{

	public MockSerializationService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean saveSessions(final Map<String, PickupSession> sessions) throws PickupBotException {
		return true;
	}
	
	@Override
	public Map<String, PickupSession> loadSessions() throws PickupBotException {
		return Collections.emptyMap();
	}
}
