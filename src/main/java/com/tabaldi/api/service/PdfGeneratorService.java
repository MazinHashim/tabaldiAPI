package com.tabaldi.api.service;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Map;

@Service
public interface PdfGeneratorService {

    void generatePdf(String htmlTemplatePath, Map<String, Object> data, String outputPath) throws FileNotFoundException;
}
