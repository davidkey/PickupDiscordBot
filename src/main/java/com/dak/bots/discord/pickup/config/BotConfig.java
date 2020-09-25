package com.dak.bots.discord.pickup.config;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class BotConfig {

	@Bean
	public JDA getJDA(@Value("${bot.token}") final String token, @Value("${bot.commandString}") final String commandString) throws InterruptedException, LoginException {
		log.trace("getJDA({})", token);
		
		final JDABuilder builder = new JDABuilder(token);
		builder.setActivity(Activity.playing(commandString + " help"));
		final JDA jda = builder.build();
		jda.awaitReady();
		
		return jda;
	}

}
