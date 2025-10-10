package com.javastudy.my_project_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.javastudy.my_project_backend.entity.dto.Account;
import com.javastudy.my_project_backend.entity.vo.request.ConfirmResetVO;
import com.javastudy.my_project_backend.entity.vo.request.EmailRegisterVO;
import com.javastudy.my_project_backend.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    //type：注册、改邮箱、重置密码
    String registerEmailVerifyCode(String type, String email, String ip);
    //注册
    String registerEmailAccount(EmailRegisterVO vo);
    //重置密码
    String resetConfirm(ConfirmResetVO vo);
    String resetEmailAccountPassword(EmailResetVO vo);
}
