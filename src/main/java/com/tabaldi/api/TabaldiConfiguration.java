package com.tabaldi.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tabaldi.configuration")
@Getter
@Setter
public class TabaldiConfiguration {
    private String hostUsername;
    private String hostPassword;
    private String hostIpAddress;
    private String hostProductImageFolder;
    private String hostVendorImageFolder;
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String sessionTokenExpiration;
    private String otpExpirationMin;
    private String otpResendTimesLimit;
    private String jwtSecretKey;

}
