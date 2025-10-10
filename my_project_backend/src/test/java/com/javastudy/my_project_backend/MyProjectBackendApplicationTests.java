package com.javastudy.my_project_backend;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MyProjectBackendApplicationTests {

    @Resource
    PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        System.out.println(passwordEncoder.encode("654321").equals("$10$uL/jd29oQzggxgSrWalIv.aLRY3oNhRndD/pDUPbHqdJOBx4xVVIO"));
    }

}
