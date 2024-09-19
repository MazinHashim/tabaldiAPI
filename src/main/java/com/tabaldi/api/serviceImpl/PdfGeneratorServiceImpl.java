package com.tabaldi.api.serviceImpl;
import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Invoice;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.service.PdfGeneratorService;
import com.tabaldi.api.utils.GenericMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    private final TabaldiConfiguration configuration;

    public byte[] generatePdf(Invoice invoice, boolean idAuthGenerated) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            String imgFileName = "src/main/resources/rateenalogo.png";
            PDImageXObject pdImage = PDImageXObject.createFromFile(imgFileName, document);
            float imgOffset = 20f;
            float headerPosition = 670;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, imgOffset, imgOffset, 60, 60);
            contentStream.drawImage(pdImage, 450, headerPosition, 140, 140);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 42);
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("INVOICE");
            contentStream.endText();

            float infoPosition = 680;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, infoPosition);
            contentStream.showText("Invoice Number: " + invoice.getInvoiceNumber());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Invoice Amount: " + invoice.getSummary().getTotal());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Customer Name: " + invoice.getOrder().getCustomer().getFirstName()+" "+
                    invoice.getOrder().getCustomer().getLastName());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Customer Phone: " + invoice.getOrder().getCustomer().getUser().getPhone());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Issue Date: " + invoice.getFIssueDate());
            contentStream.endText();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(350, infoPosition);
            contentStream.showText("Order Number: " + invoice.getOrder().getOrderNumber());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Invoice Status: " + invoice.getStatus().name());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Vendor Name: " + invoice.getOrder().getVendor().getFullName());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Order Status: " + invoice.getOrder().getStatus().name());
            contentStream.newLineAtOffset(0, -17);
            contentStream.showText("Payment Method: " + invoice.getPaymentMethod().name());
            contentStream.endText();

            // Draw the table header
            float yPosition = 570;
            float margin = 50;
            float tableWidth = 500;
            float rowHeight = 20;
            float cellMargin = 5;

            // Define column widths
            float[] columnWidths = {270, 85, 85, 100};

            // Draw table header
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths, new String[]{"Item", "Quantity", "Price", "Total"});

            yPosition -= rowHeight;

            // Draw table content

            List<CartItem> items = invoice.getOrder().getCartItems();

            for (CartItem item : items) {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                String[] itemData = {
                        item.getProduct().getName(),
                        String.valueOf(item.getQuantity()),
                        String.valueOf(item.getPrice()),
                        String.valueOf(item.getPrice()*item.getQuantity())
                };
                drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths, itemData);
                yPosition -= rowHeight;
                if(item.getOptionsCollection()!=null){
                    item.setSelectedOptions(GenericMapper
                            .jsonToListObjectMapper(item.getOptionsCollection(), Option.class));
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    for (Option option: item.getSelectedOptions()){
                        String[] optionData = {
                                option.getName(),
                                "_",
                                String.valueOf(option.getFee()!=null?option.getFee():"_"),
                                String.valueOf(option.getFee()!=null?option.getFee():"_")
                        };
                        drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths, optionData);
                        yPosition -= rowHeight;
                    }
                }
            }

            // Draw total Details row
            yPosition -= rowHeight;
            tableWidth = 220;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            margin = 330;
            columnWidths = new float[]{160, 0, 0, 40};
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths,
                    new String[]{"Shipping", invoice.getSummary().getShippingCost()+" AED", "", ""});
            yPosition -= rowHeight;
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths,
                    new String[]{"Tax(VAT)", invoice.getSummary().getTaxes()+" AED", "", ""});
            yPosition -= rowHeight;
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths,
                    new String[]{"Discount", invoice.getSummary().getDiscount()+" AED", "", ""});
            yPosition -= rowHeight;
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths,
                    new String[]{"Subtotal", invoice.getSummary().getSubtotal()+" AED", "", ""});
            yPosition -= rowHeight;
            drawTableRow(contentStream, margin, yPosition, tableWidth, rowHeight, columnWidths,
                    new String[]{"Total", invoice.getSummary().getTotal()+" AED", "", ""});

            // Close the content stream
            contentStream.close();

            if(!idAuthGenerated) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                document.save(outputStream);
                return outputStream.toByteArray();
            }
            String pathURL = configuration.getInvoicePdfFolder() + "invoice_" + invoice.getInvoiceNumber() + ".pdf";
            File file = new File(pathURL);
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            document.save(outputStream);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
    private void drawTableRow(PDPageContentStream contentStream, float x, float y, float tableWidth, float rowHeight, float[] columnWidths, String[] content) throws IOException {
        // Draw borders for the row
        drawRowBorders(contentStream, x, y, tableWidth, rowHeight, columnWidths.length);

        // Add the content
        float textX = x + 5;
        float textY = y - 15; // Adjust for text height

        for (int i = 0; i < content.length; i++) {
            float columnWidth = columnWidths[i];
            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(content[i]);
            contentStream.endText();

            // Move to the next column
            textX += columnWidth;
        }
    }

    private void drawRowBorders(PDPageContentStream contentStream, float x, float y, float tableWidth, float rowHeight, int columns) throws IOException {
        float columnWidth = tableWidth / columns;

        // Draw the top border of the row
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + tableWidth, y);
        contentStream.stroke();

        // Draw the bottom border of the row
        contentStream.moveTo(x, y - rowHeight);
        contentStream.lineTo(x + tableWidth, y - rowHeight);
        contentStream.stroke();

//        // Draw vertical lines for each column
//        for (int i = 0; i <= columns; i++) {
//            contentStream.moveTo(x + i * columnWidth, y);
//            contentStream.lineTo(x + i * columnWidth, y - rowHeight);
//            contentStream.stroke();
//        }
    }
}
