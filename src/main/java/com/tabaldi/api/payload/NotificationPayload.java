package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPayload {
    @NotNull
    @NotEmpty
    private String token;
    @NotNull
    @NotEmpty
    private String title;
    @NotNull
    @NotEmpty
    private String body;
    private String image;
    private Map<String, String> data;
}
