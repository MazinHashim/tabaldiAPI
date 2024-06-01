package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.payload.GetManyFileDataPayload;
import com.tabaldi.api.response.GetManyFileDataResponse;
import com.tabaldi.api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
@Primary
public class ServerStorageServiceImpl implements FileStorageService {
    private final MessageSource messageSource;


    @Override
    public Boolean save(List<FileDataObject> files) {
        Boolean response = false;

        try {
            Iterator var3 = files.iterator();

            while(var3.hasNext()) {
                FileDataObject file = (FileDataObject)var3.next();
                file.getMultipartFile().transferTo(new File(file.getFilePath()));
            }
            response = true;
        } catch (Exception var8) {
            System.out.println(var8.getMessage());
        }

        return response;
    }

    @Override
    public byte[] fetch(String filePath) {
        byte[] data = ("").getBytes();
        if (filePath != null) {
            try {
                data = Files.readAllBytes(new File(filePath).toPath());
            } catch (Exception var7) {
                System.out.println(var7.getMessage());
            }
        }

        return data;
    }

    @Override
    public Boolean remove(List<String> filePathList) {
        Boolean response = false;

        try {
            Iterator var3 = filePathList.iterator();

            while(var3.hasNext()) {
                String filePath = (String)var3.next();
                new File(filePath).delete();
            }

            response = true;
        } catch (Exception var8) {
            System.out.println(var8.getMessage());
        }

        return response;
    }

    public List<GetManyFileDataResponse.FDObject> fetchMany(GetManyFileDataPayload payload) {
        List<GetManyFileDataResponse.FDObject> filesData = new ArrayList();
        List<GetManyFileDataPayload.FilePathObject> filePaths = payload.getFilePaths();
        if (filePaths != null && filePaths.size() > 0) {
            filePaths.forEach((fpObj) -> {
                filesData.add(new GetManyFileDataResponse.FDObject(fpObj.getId(), Base64.getEncoder().encodeToString(this.fetch(new String(Base64.getDecoder().decode(fpObj.getPath().getBytes()))))));
            });
        }
        return filesData;
    }
}
