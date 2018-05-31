package com.seckill;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReSetDao {

	@Update("update seckill set number = 101 where seckill_id = 1001")
	public void resetId();
	
	@Delete("delete from success_killed")
	public void deleteSuccess();
}
