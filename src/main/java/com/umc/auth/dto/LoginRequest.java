package com.umc.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 요청 DTO
public record LoginRequest(String nickname, String password) {}