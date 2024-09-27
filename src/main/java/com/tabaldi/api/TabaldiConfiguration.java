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
    private String hostAdsImageFolder;

    private String smsGatewayUsername;
    private String smsGatewayPassword;
    private String smsGatewaySenderId;
    private String smsGatewayEndpointUrl;

    @Setter
    private String sessionTokenExpiration = "7776000";

    @Setter
    private String otpExpirationMin = "1";

    @Setter
    private String otpResendTimesLimit = "5";

    private String jwtSecretKey;

    private String myfatoorahApiTestKey;
    private String myfatoorahApiLiveKey;
    private String myfatoorahTestBaseUrl;
    private String myfatoorahLiveBaseUrl;

    private String emailUsername;
    private String emailPassword;

    private String activeProfile;
    private String invoicePdfFolder;
}
