package org.estg.ipp.pt.Controller;

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
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) throws IOException {

        // Generate the PDF in memory
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        logService.generatePdfReport(startDate, endDate, byteArrayOutputStream);

        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

        // Set up the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=log_report" +
                startDate.toString() + "-" + endDate.toString() + ".pdf");

        // Return the response entity with the PDF content
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}