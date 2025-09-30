package com.javastudy.my_project_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.javastudy.my_project_backend.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
}
