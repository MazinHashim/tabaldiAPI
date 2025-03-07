package com.tabaldi.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SendOtpResponse extends GenericResponse{
    private String expireOn;
    private String phone;
    private String keyRef;
}
