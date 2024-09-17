package com.tabaldi.api.service;

import com.tabaldi.api.model.Invoice;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface PdfGeneratorService {

    byte[] generatePdf(Invoice invoice, boolean idAuthGenerated) throws IOException;
}
