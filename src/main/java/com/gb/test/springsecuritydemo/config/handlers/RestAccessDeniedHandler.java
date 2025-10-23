package com.gb.test.springsecuritydemo.config.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gb.test.springsecuritydemo.enums.ResponseCodeEnum; // 导入
import com.gb.test.springsecuritydemo.model.ResultVO; // 导入
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
//403处理器
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {


    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. 创建统一的 ResultVO
        ResultVO<?> result = ResultVO.fail(ResponseCodeEnum.FORBIDDEN.getCode(),
                accessDeniedException.getMessage());

        // 3. 序列化并返回
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}