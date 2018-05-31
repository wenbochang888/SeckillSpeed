package com.seckill;

import java.util.Date;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqSender {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	public void send(String id) {
		amqpTemplate.convertAndSend(RabbitConfig.QUEUE, id);
		System.out.println("生产者生产了一个消息： " + id + "  " + new Date().getTime());
	}
	
}








