package com.example.ewaste.util;

import com.example.ewaste.model.PickupRequest;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    public static byte[] generateCompletedRequestsReport(List<PickupRequest> requests, String title) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph heading = new Paragraph(title, titleFont);
            heading.setAlignment(Element.ALIGN_CENTER);
            document.add(heading);
            document.add(Chunk.NEWLINE);

            // Table with 9 columns
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);

            // Headers
            String[] headers = {"Sr.No", "User Name", "Email", "User Mobile", "Device", "Model",
                    "Pickup Person", "Pickup Mobile"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Rows
            int srNo = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (PickupRequest req : requests) {
                table.addCell(String.valueOf(srNo++));
                table.addCell(req.getUser() != null ? req.getUser().getUsername() : "-");
                table.addCell(req.getUser() != null ? req.getUser().getEmail() : "-");
                table.addCell(req.getMobileNo() != null ? req.getMobileNo() : "-");
                table.addCell(req.getDeviceType() != null ? req.getDeviceType().name() : "-");
                table.addCell(req.getModel() != null ? req.getModel() : "-");
                table.addCell(req.getAssignedPickupPerson() != null ? req.getAssignedPickupPerson().getName() : "-");
                table.addCell(req.getAssignedPickupPerson() != null ? req.getAssignedPickupPerson().getMobile() : "-");
               ;
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
