package com.dak.bots.discord.pickup.service;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.exception.PickupBotException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SerializationService {
	
	private final ObjectMapper objectMapper;
	
	public SerializationService() {
		this.objectMapper = new ObjectMapper();
	}

	public Boolean saveSessions(final Map<String, PickupSession> sessions) throws PickupBotException {
		if(sessions != null && !sessions.isEmpty()) {
			log.debug("saving outstanding sessions ({})", sessions.size());

			try {
				final File outputFile = new File("sessions.json");
				if(outputFile.exists()) {
					outputFile.delete();
				}
				
				objectMapper.writeValue(outputFile, sessions);
			} catch (Exception e) {
				throw new PickupBotException("failed to serialize sessions!", e);
			}

			return true;
		}

		return false;
	}
	
	public Map<String, PickupSession> loadSessions() throws PickupBotException {
		try {
			objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return objectMapper.readValue(new File("sessions.json"), new TypeReference<Map<String, PickupSession>>() {});
		} catch (Exception e) {
			throw new PickupBotException("failed to de-serialize sessions!", e);
		}
	}

}
