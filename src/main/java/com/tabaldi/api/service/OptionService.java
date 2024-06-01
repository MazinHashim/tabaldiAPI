package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.payload.OptionPayload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface OptionService {
    Option getOptionById(Long optionId) throws TabaldiGenericException;
    Option saveOptionInfo(OptionPayload payload) throws TabaldiGenericException, IOException;
    Boolean deleteOptionById(Long optionId) throws TabaldiGenericException;

    List<Option> getProductOptionsList(Long productId) throws TabaldiGenericException, IOException;
}
