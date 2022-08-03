package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author swh
 * @creat 2022/7/28
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
