package com.tabaldi.api.serviceImpl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.payload.GetManyFileDataPayload;
import com.tabaldi.api.response.GetManyFileDataResponse;
import com.tabaldi.api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
//@Primary
public class FileStorageServiceImpl implements FileStorageService {
    private Session session;
    private Channel channel;
    private ChannelSftp sftp;
    private final MessageSource messageSource;
    private final TabaldiConfiguration configuration;

    private ChannelSftp getSFTP() {
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            this.session = jsch.getSession(configuration.getHostUsername(), configuration.getHostIpAddress(), 22);
            this.session.setPassword(configuration.getHostPassword());
            this.session.setConfig(config);
            this.session.connect();
            System.out.println("SSh Connected");
            this.channel = this.session.openChannel("sftp");
            this.channel.connect();
            this.sftp = (ChannelSftp)this.channel;
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return this.sftp;
    }

    private void closeConnection() {
        if (this.channel != null && this.channel.isConnected()) {
            this.channel.disconnect();
        }

        if (this.session != null && this.session.isConnected()) {
            this.session.disconnect();
        }

        if (this.sftp != null && this.sftp.isConnected()) {
            this.sftp.disconnect();
        }

        System.out.println("SSh Closed");
    }

    @Override
    public Boolean save(List<FileDataObject> files) {
        Boolean response = false;

        try {
            Iterator var3 = files.iterator();

            while(var3.hasNext()) {
                FileDataObject file = (FileDataObject)var3.next();
                this.getSFTP().put(file.getMultipartFile().getInputStream(), file.getFilePath());
            }

            response = true;
        } catch (Exception var8) {
            System.out.println(var8.getMessage());
        } finally {
            this.closeConnection();
        }

        return response;
    }

    @Override
    public byte[] fetch(String filePath) {
        byte[] data = ("").getBytes();
        if (filePath != null) {
            try {
                data = IOUtils.toByteArray(this.getSFTP().get(filePath));
            } catch (Exception var7) {
                System.out.println(var7.getMessage());
            } finally {
                this.closeConnection();
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
                this.getSFTP().rm(filePath);
            }

            response = true;
        } catch (Exception var8) {
            System.out.println(var8.getMessage());
        } finally {
            this.closeConnection();
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
