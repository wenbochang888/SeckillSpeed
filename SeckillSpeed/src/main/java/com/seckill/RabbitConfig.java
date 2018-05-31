package com.seckill;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
	
	public static final String QUEUE = "seckill";
	
	@Bean
	public Queue queue() {
		return new Queue(QUEUE, true);
	}
}












