package org.estg.ipp.pt.Services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * Generates a PDF report of logs and saves it to the local file system.
     *
     * @param startDate The start date of the logs to be included.
     * @param endDate   The end date of the logs to be included.
     * @param filePath  The path where the PDF file will be saved.
     * @throws IOException If there is an IO error while generating or saving the PDF.
     */
    public void generatePdfReport(LocalDateTime startDate, LocalDateTime endDate, String filePath) throws IOException {
        // Fetch logs from repository
        List<Log> logs = logRepository.findByDateRange(startDate, endDate);

        // Create a new PDF document
        PDDocument document = new PDDocument();

        // Create a page in the document
        PDPage page = new PDPage();
        document.addPage(page);

        // Prepare content stream to write content on the page
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, 750);

        // Add title
        contentStream.showText("Log Report");
        contentStream.newLineAtOffset(0, -20);

        // Add headers for the log table
        contentStream.showText("Date                          | Tag         | Message");
        contentStream.newLineAtOffset(0, -20);

        // Add logs to the content stream
        for (Log log : logs) {
            String logEntry = String.format("%-30s | %-10s | %s", log.getDateTime(), log.getTag(), log.getMessage());
            contentStream.showText(logEntry);
            contentStream.newLineAtOffset(0, -20);
        }

        contentStream.endText();
        contentStream.close();

        // Save the document to the local file system
        document.save(new File(filePath));
        document.close();
    }


    private void handleGeneratePdfReport(Socket clientSocket, LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<Log> logs = logRepository.findByDateRange(startDate, endDate);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("Log Report");
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Date                          | Tag         | Message");
        contentStream.newLineAtOffset(0, -20);

        for (Log log : logs) {
            String logEntry = String.format("%-30s | %-10s | %s", log.getDateTime(), log.getTag(), log.getMessage());
            contentStream.showText(logEntry);
            contentStream.newLineAtOffset(0, -20);
        }

        contentStream.endText();
        contentStream.close();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.save(byteArrayOutputStream);
        document.close();

        clientSocket.getOutputStream().write(byteArrayOutputStream.toByteArray());
        clientSocket.getOutputStream().flush();
    }
}
