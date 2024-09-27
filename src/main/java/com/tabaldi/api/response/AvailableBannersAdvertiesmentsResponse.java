package com.tabaldi.api.response;

import java.util.List;
import java.util.Map;

import com.tabaldi.api.model.Advertisement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableBannersAdvertiesmentsResponse extends GenericResponse {
    private String event;
    private List<Advertisement> advertisements;
    private Map<String, String> availableBanners;

}
