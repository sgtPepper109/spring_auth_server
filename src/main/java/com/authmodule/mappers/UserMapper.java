package com.authmodule.mappers;

import com.authmodule.dto.SignupDto;
import com.authmodule.dto.UserDto;
import com.authmodule.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userDto(User user);

    @Mapping(target = "password", ignore = true)
    User signupUser(SignupDto signupDto);
}
