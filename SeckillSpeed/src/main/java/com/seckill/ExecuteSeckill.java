package com.seckill;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExecuteSeckill {
	
	private static final String stock = "stock:";
	
	@Autowired
	SeckillDao seckillDao;
	@Autowired
	RedisTemplate<Object, Object> redis;
	@Autowired
	StringRedisTemplate stringRedis;
	@Autowired
	ReSetDao reSetDao;
	@Autowired
	RabbitMqSender sender;
	
	//控制层
	@RequestMapping(value = "/{id}/execute", method = RequestMethod.GET)
	public String execute(@PathVariable(value = "id") String id) {
		
		if (id == null) {
			return "id为空,秒杀失败";
		}
		String flag = executSeckill(id);
		return flag;
	}
	
	//dao层
	public Seckill getSeckillById(String id) {
		return seckillDao.getSeckillById(id);
	}
	
	//减少库存
	public int reduceNumber(String id) {
		
		return seckillDao.reduceNumber(id);
	}
	//插入秒杀记录
	public void insertSuccessKilled(String id, String phone) {
		seckillDao.insertSuccessKilled(id, phone);
	}
	
	//序列化
	public Seckill sizeliable(String id) {
		
		Seckill seckill = getSeckillById(id);
		redis.opsForValue().set("seckill:" + id, seckill);
		return seckill;
	}
	
	//反序列化
	public Seckill desizeliable(String id) {
		
		Seckill seckill = (Seckill) redis.opsForValue().get("seckill:" + id);
		return seckill;
	}
	
	//service层
	public String executSeckill(String id) {

		Seckill seckill = desizeliable(id);
		long num = 0;
		if (seckill == null) {
			// 没有命中
			seckill = sizeliable(id);
			// 将商品数量加载到redis里面去
			System.out.println("没有命中  " + seckill);
			stringRedis.opsForValue().set(stock + id, seckill.getNumber() + "");
		}
		num = stringRedis.opsForValue().increment(stock + id, -1);
		if (num < 0) {
			// 秒杀失败
			// 可以拦截99%的无效秒杀，降低流量
			System.out.println("流量被拦截了");
			return "肯定失败，流量被拦截了";
		}

		// 这里是一个事务
		// 库存减少1，插入成功记录
		//优化 入队
		sender.send(id);
		return "入队中，还不清楚";
	}
	
	@Transactional
	public boolean executeSmall(String id) throws RuntimeException {
		
		int state = reduceNumber(id);
		if (state <= 0) {
			throw new RuntimeException("库存不足");
		}
		String x = String.valueOf(System.nanoTime()).substring(5);
		insertSuccessKilled(id,  x + new Random().nextInt(1000));
		return true;
	}
	
	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	public String reset() {
		
		System.out.println("isEnableDefaultSerializer  " + redis.isEnableDefaultSerializer());
		
		//update sekill 1001 为101件商品
		reSetDao.resetId();
		//删除成功记录
		reSetDao.deleteSuccess();
		//清空redis数据
		stringRedis.delete("stock:1001");
		redis.delete("seckill:1001");
		
		return "重置成功";
	}
}










