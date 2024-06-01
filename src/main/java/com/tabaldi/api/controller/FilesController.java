package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.payload.GetManyFileDataPayload;
import com.tabaldi.api.response.FileDataResponse;
import com.tabaldi.api.response.GetManyFileDataResponse;
import com.tabaldi.api.service.FileStorageService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FilesController {

    private final MessageSource messageSource;

    private final FileStorageService fileStorageService;

    @GetMapping(value = "/get/data/{filePath}")
    public @ResponseBody
    FileDataResponse getFileData(@PathVariable("filePath") final String filePath) throws Exception, Throwable {
        if (StringUtils.hasText(filePath)) {
            String data = Base64.getEncoder().encodeToString(
                    fileStorageService.fetch(new String(Base64.getDecoder().decode(filePath.getBytes()))));
            if(data.isEmpty()) {
                String noFileMessage = messageSource.getMessage("error.no.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, noFileMessage);
            }
            else {
                String responseMessage = MessagesUtils.getFetchMessage(messageSource, "File", "الملف");
                return FileDataResponse.builder()
                        .message(responseMessage)
                        .data(data)
                        .build();
            }
        } else {
            String requiredImageUploadMessage = messageSource.getMessage("error.required.upload.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredImageUploadMessage);
        }
    }
    @PostMapping(value = "/get/all/data")
    public @ResponseBody
    GetManyFileDataResponse getManyFileData(@RequestBody final GetManyFileDataPayload payload) throws Exception, Throwable {
        List<GetManyFileDataResponse.FDObject> filesData = fileStorageService.fetchMany(payload);
        boolean isEmpty = filesData.stream().filter(file->!file.getData().isEmpty()).collect(Collectors.toList())
                .isEmpty();
        if (payload.getFilePaths() == null || isEmpty) {
            String noFileMessage = messageSource.getMessage("error.no.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, noFileMessage);
        } else {
            String responseMessage = MessagesUtils.getFetchMessage(messageSource, "Files", "الملفات");
            return GetManyFileDataResponse.builder()
                    .message(responseMessage)
                    .filesData(filesData)
                    .build();
        }
    }
}
