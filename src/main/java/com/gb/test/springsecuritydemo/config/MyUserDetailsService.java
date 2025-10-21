package com.gb.test.springsecuritydemo.config;

import com.gb.test.springsecuritydemo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 1. 把它标记为一个 Spring Service 组件
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 2. 通过构造函数注入 UserRepository
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 3. 使用 Repository 从数据库查询用户
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        // 4. 如果找不到用户，必须抛出 UsernameNotFoundException
        //    如果找到了，就直接返回那个 User 对象（因为它实现了 UserDetails）
    }
}
