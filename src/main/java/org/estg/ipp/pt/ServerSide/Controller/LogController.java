package org.estg.ipp.pt.ServerSide.Controller;

import com.itextpdf.text.DocumentException;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.ServerSide.Services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * O controlador {@code LogController} gerencia as requisições HTTP relacionadas a relatórios de logs.
 *
 * <p>Este controlador fornece um endpoint para download de relatórios em PDF baseados nos logs. O PDF pode ser gerado
 * de acordo com diferentes filtros: por tag, por intervalo de datas ou ambos.</p>
 *
 * <p>O relatório gerado é retornado como um arquivo PDF para o cliente, com a possibilidade de personalizar os filtros
 * através dos parâmetros da requisição.</p>
 */
@RestController
public class LogController {

    /**
     * O serviço de logs que contém a lógica para gerar os relatórios.
     */
    @Autowired
    private LogService logService;

    /**
     * Endpoint para gerar e fazer o download de um relatório em PDF com base em filtros de tag e intervalo de datas.
     *
     * <p>O relatório pode ser filtrado por tag, por intervalo de datas, ou por ambos. Se nenhum filtro for fornecido,
     * uma exceção será lançada.</p>
     *
     * @param tag A tag de filtro para o relatório (opcional).
     * @param startDate A data inicial do intervalo (opcional).
     * @param endDate A data final do intervalo (opcional).
     * @return Um {@link ResponseEntity} contendo o arquivo PDF gerado.
     * @throws IOException Se ocorrer um erro na criação do relatório em PDF.
     * @throws DocumentException Se ocorrer um erro ao gerar o documento PDF.
     * @throws IllegalArgumentException Se nenhum parâmetro (tag, startDate, endDate) for fornecido.
     */
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