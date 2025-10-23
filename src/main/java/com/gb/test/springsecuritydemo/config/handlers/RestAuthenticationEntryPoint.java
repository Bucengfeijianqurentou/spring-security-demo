package com.gb.test.springsecuritydemo.config.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gb.test.springsecuritydemo.enums.ResponseCodeEnum; // 导入我们的枚举
import com.gb.test.springsecuritydemo.model.ResultVO; // 导入我们的 ResultVO
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
//401处理器
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. 创建统一的 ResultVO
        // 注意：我们把异常信息 authException.getMessage() 作为 data 传给了前端，方便调试
        ResultVO<?> result = ResultVO.fail(ResponseCodeEnum.UNAUTHORIZED.getCode(),
                authException.getMessage());

        // 3. 使用 ObjectMapper 将对象序列化为 JSON 字符串并写入响应
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}