package com.javastudy.my_project_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.my_project_backend.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
