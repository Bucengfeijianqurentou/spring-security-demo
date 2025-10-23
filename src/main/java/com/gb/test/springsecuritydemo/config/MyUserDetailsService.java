package com.gb.test.springsecuritydemo.config;

import com.gb.test.springsecuritydemo.entity.User; // 导入我们自己的 User 实体
import com.gb.test.springsecuritydemo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库中查询我们的 User 实体
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. **关键转换**：
        //    从我们的 User 实体中获取信息，
        //    并构建一个 Spring Security 认识的 'org.springframework.security.core.userdetails.User' 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                // 3. 将我们的 "role" 字符串，转换成 Security 需要的 "权限集合"
                //    注意：我们必须在角色名前加上 "ROLE_" 前缀，这是 Spring Security 的约定
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}