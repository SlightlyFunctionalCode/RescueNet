package org.estg.ipp.pt.Services;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public void generatePdfReportByDateRangeAndTag(LocalDateTime startDate, LocalDateTime endDate, TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByTagAndDateRange(tag, startDate, endDate);

        // Create a new PDF document
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add title
        Paragraph title = new Paragraph("Log Report")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Create table
        float[] columnWidths = {2, 1, 3}; // Adjust column widths as needed
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100));

        // Add headers for the log table
        addTableHeader(table, "Date");
        addTableHeader(table, "Tag");
        addTableHeader(table, "Message");

        // Add logs to the table
        for (Log log : logs) {
            addTableRow(table, log.getDateTime(), log.getTag(), log.getMessage());
        }

        // Add the table to the document
        document.add(table);

        // Close the document
        document.close();
    }

    public void generatePdfReportByTag(TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByTag(tag);

        // Create a new PDF document
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add title
        Paragraph title = new Paragraph("Log Report")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Create table
        float[] columnWidths = {2, 1, 3}; // Adjust column widths as needed
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100));

        // Add headers for the log table
        addTableHeader(table, "Date");
        addTableHeader(table, "Tag");
        addTableHeader(table, "Message");

        // Add logs to the table
        for (Log log : logs) {
            addTableRow(table, log.getDateTime(), log.getTag(), log.getMessage());
        }

        // Add the table to the document
        document.add(table);

        // Close the document
        document.close();
    }

    public void generatePdfReportByDateRange(LocalDateTime startDate, LocalDateTime endDate, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByDateRange(startDate, endDate);

        // Create a new PDF document
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add title
        Paragraph title = new Paragraph("Log Report")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Create table
        float[] columnWidths = {2, 1, 3}; // Adjust column widths as needed
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100));

        // Add headers for the log table
        addTableHeader(table, "Date");
        addTableHeader(table, "Tag");
        addTableHeader(table, "Message");

        // Add logs to the table
        for (Log log : logs) {
            addTableRow(table, log.getDateTime(), log.getTag(), log.getMessage());
        }

        // Add the table to the document
        document.add(table);

        // Close the document
        document.close();
    }

    private void addTableHeader(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFontSize(12)
                        .setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        table.addCell(cell);
    }

    private void addTableRow(Table table, String date, TagType tag, String message) {
        Color tagColor = getTagColor(tag);

        table.addCell(new Cell()
                .add(new Paragraph(tag.toString()))
                .setTextAlignment(TextAlignment.LEFT)
                .setFontColor(tagColor)
                .setBold()
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));

        table.addCell(new Cell()
                .add(new Paragraph(date))
                .setTextAlignment(TextAlignment.LEFT)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));

        table.addCell(new Cell()
                .add(new Paragraph(message))
                .setTextAlignment(TextAlignment.LEFT)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
    }

    private Color getTagColor(TagType tag) {
        Map<TagType, Color> tagColorMap = new HashMap<>();
        tagColorMap.put(TagType.INFO, ColorConstants.GRAY);
        tagColorMap.put(TagType.ERROR, ColorConstants.RED);
        tagColorMap.put(TagType.ALERT, ColorConstants.ORANGE);
        tagColorMap.put(TagType.CRITICAL, ColorConstants.DARK_GRAY);
        tagColorMap.put(TagType.SUCCESS, ColorConstants.GREEN);
        tagColorMap.put(TagType.FAILURE, ColorConstants.PINK);
        tagColorMap.put(TagType.ACCESS, ColorConstants.BLUE);
        tagColorMap.put(TagType.USER_ACTION, ColorConstants.CYAN);
        tagColorMap.put(TagType.DATABASE, ColorConstants.YELLOW);
        tagColorMap.put(TagType.NETWORK, ColorConstants.MAGENTA);
        tagColorMap.put(TagType.SECURITY, ColorConstants.BLACK);

        return tagColorMap.getOrDefault(tag, ColorConstants.LIGHT_GRAY);
    }
}