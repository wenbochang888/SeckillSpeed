package com.seckill;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@RabbitListener(queues = RabbitConfig.QUEUE)
public class RabbitMqReceiver {

	@Autowired
	ExecuteSeckill exe;
	
	@RabbitHandler
	public void receive(String id) throws JsonParseException, JsonMappingException, IOException {
		
		System.out.println("消费者收到了一个消息: " + id + "  " + new Date().getTime());
		executeSmallByRabbit(id);
	}
	
	@Transactional
	public boolean executeSmallByRabbit(String id) throws RuntimeException {
		
		int state = exe.reduceNumber(id);
		if (state <= 0) {
			throw new RuntimeException("库存不足");
		}
		String x = String.valueOf(System.nanoTime()).substring(5);
		exe.insertSuccessKilled(id,  x + new Random().nextInt(1000));
		return true;
	}
}










