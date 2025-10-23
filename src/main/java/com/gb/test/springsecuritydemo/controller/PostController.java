package com.gb.test.springsecuritydemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts") // 模块的根路径
public class PostController {

    /**
     * 假设这个接口是公开的，比如“热门文章列表”
     */
    @GetMapping("/public/list")
    public String getPublicPosts() {
        return "这是任何人都可以看的热门文章列表。";
    }

    /**
     * 假设这个接口是需要登录的，比如“我收藏的文章”
     */
    @GetMapping("/protected/my-favorites")
    public String getMyFavoritePosts() {
        // 因为 Security 已经验证过了，
        // 在这里你甚至可以安全地获取当前用户信息
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // String currentUsername = auth.getName();
        return "这是登录后才能看到的【我收藏的文章】。";
    }
}
