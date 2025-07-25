package com.batuhan.feedback360.model.converter;

import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.response.ParticipantResponse;
import com.batuhan.feedback360.model.response.UserDetailResponse;
import com.batuhan.feedback360.model.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public ParticipantResponse toParticipantResponse(User user) {
        if (user == null) {
            return null;
        }

        return ParticipantResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }

    public UserDetailResponse toUserDetailResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .isActive(user.getIsActive())
            .role(user.getRole())
            .isAdmin(user.getIsAdmin())
            .build();
    }
}