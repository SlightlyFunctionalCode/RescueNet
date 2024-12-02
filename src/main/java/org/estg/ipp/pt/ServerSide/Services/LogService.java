package org.estg.ipp.pt.ServerSide.Services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.ServerSide.Repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {
    @Autowired
    private LogRepository logRepository;  // Ensure this is injected correctly by Spring

    public void saveLog(Log log) {
        // Save the user in the database
        logRepository.save(log);
    }

    public List<Log> findAllLogs() {
        return logRepository.findAll();
    }

    public List<Log> findLogsByTag(TagType tag) {
        return logRepository.findByTag(tag);
    }

    public List<Log> findLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByDateRange(startDate, endDate);
    }

    public List<Log> findLogsByTagAndDateRange(TagType tag, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByTagAndDateRange(tag, startDate, endDate);
    }

    public void generatePdfReportByDateRangeAndTag(LocalDateTime startDate, LocalDateTime endDate, TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByTagAndDateRange(tag, startDate, endDate);

        generatePdf(logs, byteArrayOutputStream);
    }

    public void generatePdfReportByTag(TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByTag(tag);

        generatePdf(logs, byteArrayOutputStream);
    }

    // Generate PDF by Date Range
    public void generatePdfReportByDateRange(LocalDateTime startDate, LocalDateTime endDate, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByDateRange(startDate, endDate);

        generatePdf(logs, byteArrayOutputStream);
    }

    private void generatePdf(List<Log> logs, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        // Create a new PDF document
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument();
        pdfDoc.addWriter(writer);

        document.open();

        // Add title
        Paragraph title = new Paragraph("Log Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));


        // Create table
        PdfPTable table = new PdfPTable(3); // 3 columns
        table.setWidthPercentage(100); // Table width is 100% of page

        // Set column widths
        float[] columnWidths = {1, 2, 3}; // Adjust as needed
        table.setWidths(columnWidths);

        // Add headers for the log table
        addTableHeader(table);

        // Add logs to the table
        for (Log log : logs) {
            addTableRow(table, log.getDateTime(), log.getTag(), log.getMessage());
        }

        // Add table to the document
        document.add(table);

        // Close the document
        document.close();
    }

    // Add header cells to the table
    private void addTableHeader(PdfPTable table) {
        // Tag header
        PdfPCell cell = new PdfPCell(new Phrase("Tag", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        // Date header
        cell = new PdfPCell(new Phrase("Date", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        // Message header
        cell = new PdfPCell(new Phrase("Message", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // Add a row to the table
    private void addTableRow(PdfPTable table, String date, TagType tag, String message) {
        Color tagColor = getTagColor(tag);

        // Tag cell with color
        PdfPCell tagCell = new PdfPCell(new Phrase(tag.toString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        tagCell.setBackgroundColor(new BaseColor(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue()));
        tagCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(tagCell);

        // Date cell
        table.addCell(new PdfPCell(new Phrase(date, FontFactory.getFont(FontFactory.HELVETICA, 10))));

        // Message cell
        table.addCell(new PdfPCell(new Phrase(message, FontFactory.getFont(FontFactory.HELVETICA, 10))));
    }

    // Get color based on the tag
    private Color getTagColor(TagType tag) {
        Map<TagType, Color> tagColorMap = new HashMap<>();
        tagColorMap.put(TagType.INFO, Color.GRAY);
        tagColorMap.put(TagType.ERROR, Color.RED);
        tagColorMap.put(TagType.ALERT, Color.ORANGE);
        tagColorMap.put(TagType.CRITICAL, Color.DARK_GRAY);
        tagColorMap.put(TagType.SUCCESS, Color.GREEN);
        tagColorMap.put(TagType.FAILURE, Color.PINK);
        tagColorMap.put(TagType.ACCESS, Color.BLUE);
        tagColorMap.put(TagType.USER_ACTION, Color.CYAN);
        tagColorMap.put(TagType.DATABASE, Color.YELLOW);
        tagColorMap.put(TagType.NETWORK, Color.MAGENTA);
        tagColorMap.put(TagType.SECURITY, Color.BLACK);

        return tagColorMap.getOrDefault(tag, Color.LIGHT_GRAY);
    }
}