package com.umc.domain.user.converter;

import com.umc.domain.user.dto.UserResponseDTO;
import com.umc.domain.user.entity.User;

public class UserConverter {

    public static UserResponseDTO.MyNameDTO toMyNameDTO(User user) {
        return UserResponseDTO.MyNameDTO.builder()
                .nickname(user.getNickname())
                .build();
    }
}

