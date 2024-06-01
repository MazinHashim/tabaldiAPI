package com.tabaldi.api.response;

import com.tabaldi.api.model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse extends GenericResponse {
    private boolean newUser;
    private UserEntity user;

}
