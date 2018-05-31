package com.seckill;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SeckillDao {

	public Seckill getSeckillById(@Param("id") String id);
	
	public int reduceNumber(@Param("id") String id);
	
	public void insertSuccessKilled(@Param("id") String id,
			@Param("phone") String phone);
}
