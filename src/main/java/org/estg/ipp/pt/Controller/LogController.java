package org.estg.ipp.pt.Controller;

import com.itextpdf.text.DocumentException;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/download-pdf-report")
    public ResponseEntity<ByteArrayResource> downloadPdfReport(
            @RequestParam(required = false) TagType tag,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IOException, DocumentException {

        // Generate the PDF in memory
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (tag != null && startDate != null && endDate != null) {
            // Generate report for date range and tag
            logService.generatePdfReportByDateRangeAndTag(startDate, endDate, tag, byteArrayOutputStream);
        } else if (tag != null) {
            // Generate a report for tag
            logService.generatePdfReportByTag(tag, byteArrayOutputStream);
        } else if (startDate != null && endDate != null) {
            // Generate a report for date range
            logService.generatePdfReportByDateRange(startDate, endDate, byteArrayOutputStream);
        } else {
            // Handle the case where no parameters are provided
            throw new IllegalArgumentException("At least one parameter (tag, startDate, endDate) must be provided");
        }


        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

        // Set up the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=log_report.pdf");

        // Return the response entity with the PDF content
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}