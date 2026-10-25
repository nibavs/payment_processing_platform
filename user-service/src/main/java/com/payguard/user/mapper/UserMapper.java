package com.payguard.user.mapper;

import com.payguard.user.dto.UserResponse;
import com.payguard.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring"
)
public interface UserMapper {

    @Mapping(target = "avatarUrl", source = "avatarUrl")
    UserResponse toResponse(User user, String avatarUrl);
}
