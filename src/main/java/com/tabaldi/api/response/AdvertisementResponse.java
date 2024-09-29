package com.tabaldi.api.response;

import com.tabaldi.api.model.Advertisement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementResponse extends GenericResponse {
    private String event;
    private Advertisement advertisement;
}
