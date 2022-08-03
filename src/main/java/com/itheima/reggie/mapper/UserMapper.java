package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author swh
 * @creat 2022/8/2
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
