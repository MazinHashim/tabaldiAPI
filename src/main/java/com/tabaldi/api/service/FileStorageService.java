package com.tabaldi.api.service;

import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.payload.GetManyFileDataPayload;
import com.tabaldi.api.response.GetManyFileDataResponse;

import java.util.List;

public interface FileStorageService {

    Boolean save(List<FileDataObject> files);
    byte[] fetch(String filePath);
    Boolean remove(List<String> filePathList);
    List<GetManyFileDataResponse.FDObject> fetchMany(GetManyFileDataPayload payload);
}
