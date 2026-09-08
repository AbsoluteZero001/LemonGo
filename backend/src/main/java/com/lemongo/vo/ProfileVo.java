package com.lemongo.vo;

public record ProfileVo(
        Long id,
        String username,
        String role,
        String nickname,
        String email,
        String phone,
        String avatarUrl) {
}
