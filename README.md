# Java高并发秒杀系统最终版

## 项目QPS

在自己的笔记本i5上面QPS可达800左右

在学校的i7处理器上面QPS最高达到了2900多，几乎3000，平均可以到2500上下。

瓶颈可能在机子的性能，也可能在Redis那一块，或许或许在MySql那一块也说不定

## 项目缘由

春招已经结束了，我之前写了一个Java高并发的秒杀系统，被许多面试官问到，并且提出了许多很宝贵的建议，最新有空，根据很多面试官的建议改了改我的项目。

这个项目不是一个完整的项目，仅仅只写了一个秒杀接口，非完整项目。

## 项目使用技术

项目使用了SpringBoot + Nginx + RabbitMQ + Redis集群 + MySql + Protostuff技术

## 项目坏境

jdk1.8 + eclipse + window8.1

## 项目地址

如果大家想测试一下这个接口，可以用这个网址。但QPS应该很低，因为这台服务器cpu一核，根本跑不起来并发。 <a href="http://120.78.159.149:8080/springbootstudy/1001/execute" target="_blank">点我即可</a>

## 高并发优化思路（Important）

![Image text](https://github.com/wenbochang888/SeckillSpeed/blob/master/img/sum.png)

1：首先先将商品的信息以及商品的数量加载到Redis缓存里面去

2：当Nginx收到请求，Nginx做一个负载均衡进行转发。这里是根据ip_hash进行转发，后台可以建立一个tomcat集群，因为我这边服务器有限，就一台服务器，看不出效果。

3：当tomcat服务器收到请求，先去Redis里面预减少库存，做一个判断，这操作可以拦截99%的无效流量，达到高并发的目的。因为Redis要拦截大多数的无效请求，所以我这里搞了一个Redis集群，六个节点，三主三从。<a href = "https://www.cnblogs.com/wenbochang/p/9060122.html" target="_blank">Redis集群搭建</a>。该步骤逻辑大致如下：

	num = stringRedis.opsForValue().increment(stock + id, -1);
	if (num < 0) {
		// 秒杀失败
		// 可以拦截99%的无效秒杀，降低流量
		System.out.println("流量被拦截了");
		return "肯定失败，流量被拦截了";
	}

4：当请求进行到了下一步。立刻入队，然后立刻返回给用户排队中，请等待。然后后台异步处理用户请求。改请求包括两个步骤，减库存，插入秒杀成功记录。
	
	//入队
	sender.send(id);

	//出队
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
			
ps. 序列化和反序列的时候用到了第三方的框架protostuff，这个序列化更快而且数据大小可以达到原来的1/5 - 1/10左右

## 再次说明

这个只有一个秒杀接口，非整个秒杀项目。但麻雀虽小五脏俱全。


