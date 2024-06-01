package com.tabaldi.api.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class FileDataObject {

	private MultipartFile multipartFile;
	private String filePath;
}