package com.tabaldi.api.serviceImpl;
import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.model.Invoice;
import com.tabaldi.api.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    private final TabaldiConfiguration configuration;

    public byte[] generatePdf(Invoice invoice) throws FileNotFoundException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            String imgFileName = "src/main/resources/rateenalogo.png";
            PDImageXObject pdImage = PDImageXObject.createFromFile(imgFileName, document);

//            int iw = pdImage.getWidth();
//            int ih = pdImage.getHeight();
            float offset = 20f;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, offset, offset, 60, 60);
            contentStream.drawImage(pdImage, 450, 650, 140, 140);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("Invoice for: " + "Mazin Hashim");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Invoice Number: " + invoice.getInvoiceNumber());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Invoice Amount: " + "10,000");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Invoice Amount: " + invoice.getSummary().getTotal());
            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
