package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.payload.OptionPayload;
import com.tabaldi.api.response.OptionResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.service.OptionService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OptionController {

    private final OptionService optionService;
    private final MessageSource messageSource;

    @GetMapping("/{optionId}")
    public @ResponseBody ResponseEntity<OptionResponse> getById (@PathVariable("optionId") Long optionId)
            throws TabaldiGenericException {
        Option option = optionService.getOptionById(optionId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Option", "التكملة");

        return ResponseEntity.ok(OptionResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .option(option).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<OptionResponse> saveOption (
            @RequestBody @Valid OptionPayload payload) throws TabaldiGenericException, IOException {

        Option option = optionService.saveOptionInfo(payload);
        String event = payload.getOptionId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Option", "التكملة", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                OptionResponse.builder()
                        .event(event)
                        .option(option)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{optionId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteOption (@PathVariable("optionId") Long optionId)
            throws TabaldiGenericException {
        Boolean isDeleted = optionService.deleteOptionById(optionId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Option", "التكملة");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }
}
