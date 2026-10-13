package com.payguard.user.mapper;

import com.payguard.user.dto.UserResponse;
import com.payguard.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface UserMapper {
    UserResponse toResponse(User user);
}
