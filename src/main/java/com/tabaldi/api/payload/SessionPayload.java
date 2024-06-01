package com.tabaldi.api.payload;

import com.tabaldi.api.model.UserEntity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionPayload {
    @NotNull
    private UserEntity user;

    @NotNull
    @NotEmpty
    private String token;
}
