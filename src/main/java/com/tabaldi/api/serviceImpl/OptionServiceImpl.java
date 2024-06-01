package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.OptionPayload;
import com.tabaldi.api.repository.OptionRepository;
import com.tabaldi.api.service.ProductService;
import com.tabaldi.api.service.OptionService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {
    private final OptionRepository optionRepository;
    private final ProductService productService;
    private final MessageSource messageSource;

    @Override
    public Option getOptionById(Long optionId) throws TabaldiGenericException {
        Optional<Option> selectedOption = optionRepository.findById(optionId);
        if(!selectedOption.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Option","التكملة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedOption.get();
    }

    @Override
    public List<Option> getProductOptionsList(Long productId) throws TabaldiGenericException, IOException {
        Product product = productService.getProductById(productId);
        List<Option> optionList = optionRepository.findByProduct(product);
        if(optionList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"options", "المكملات");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return optionList;
    }

    @Override
    public Option saveOptionInfo(OptionPayload payload) throws TabaldiGenericException, IOException {
        // update option constraints
        if(payload.getOptionId()!=null){
            Option option = this.getOptionById(payload.getOptionId());
            if(option.getProduct()!=null &&
                    option.getProduct().getProductId()!=payload.getProductId()){
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource,"Option", "التكملة");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            } else if(payload.getName()!=option.getName()){
                String changeCoordinatesMessage = messageSource.getMessage("error.change.coordinates", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeCoordinatesMessage);
            }
        }
        Product selectedProduct = productService.getProductById(payload.getProductId());
        if (payload.getOptionId() == null && this.checkIfOptionExistsPerProduct(selectedProduct, payload.getName())) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"Option", "التكملة");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, "إسم "+alreadyExistMessage);
        } else {
            Option optionParams = Option.builder()
                    .name(payload.getName().toLowerCase())
                    .product(selectedProduct)
                    .build();
            if (payload.getOptionId() != null)
                optionParams.setOptionId(payload.getOptionId());
            if(payload.getGroupFlag()!=null && !payload.getGroupFlag().isEmpty())
                optionParams.setGroupFlag(payload.getGroupFlag().toLowerCase());
            if(payload.getFee() != null)
                optionParams.setFee(payload.getFee());
            return optionRepository.save(optionParams);
        }
    }

    @Override
    public Boolean deleteOptionById(Long optionId) throws TabaldiGenericException {
        Optional<Option> optionOptional = optionRepository.findById(optionId);
        if (!optionOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Option", "العنوان");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Option option = optionOptional.get();
            optionRepository.deleteById(option.getOptionId());
            return true;
        }
    }

    private boolean checkIfOptionExistsPerProduct(Product selectedProduct, String name) {
        Optional<Option> option = optionRepository.findByProductAndName(selectedProduct.getProductId(), name);
        return option.isPresent();
    }
}
