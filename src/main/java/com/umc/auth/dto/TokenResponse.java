package com.umc.auth.dto;

// 응답 DTO
public record TokenResponse( Long id,
                             String nickname,
                             String accessToken) {}
