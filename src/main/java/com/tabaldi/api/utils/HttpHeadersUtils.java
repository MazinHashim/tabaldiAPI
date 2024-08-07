package com.tabaldi.api.utils;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

@NoArgsConstructor
public class HttpHeadersUtils {
    private static HttpHeaders headers;

    public static HttpHeaders getApplicationJsonHeader() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public static HttpHeaders getApplicationJsonSSHHeader() {
        headers = new HttpHeaders();
        headers.add("user-agent", "PostmanRuntime/7.26.8");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public static HttpHeaders getApplicationXmlHeader() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        return headers;
    }

    public static HttpHeaders getMultipartFormDataHeader() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }
}
