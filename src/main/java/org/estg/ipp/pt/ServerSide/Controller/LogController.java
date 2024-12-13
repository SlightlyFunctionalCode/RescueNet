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
 * O controlador {@code LogController} gere os pedidos HTTP relacionadas a relatórios de logs.
 *
 * <p>Este controlador fornece um endpoint para o download de relatórios em PDF baseados nos logs. O PDF pode ser gerado
 * de acordo com diferentes filtros: por tag, por intervalo de datas ou ambos.</p>
 *
 * <p>O relatório gerado é devolvido como um arquivo PDF para o cliente.</p>
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
     * Se nenhum filtro for fornecido, uma exceção será lançada.</p>
     *
     * @param tag A tag de filtro para o relatório (opcional).
     * @param startDate A data inicial do intervalo (opcional).
     * @param endDate A data final do intervalo (opcional).
     * @return Um {@link ResponseEntity} que contém o arquivo PDF gerado.
     * @throws DocumentException Se ocorrer um erro ao gerar o documento PDF.
     * @throws IllegalArgumentException Se nenhum parâmetro (tag, startDate, endDate) for fornecido.
     */
    @GetMapping("/download-pdf-report")
    public ResponseEntity<ByteArrayResource> downloadPdfReport(
            @RequestParam(required = false) TagType tag,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IllegalArgumentException, DocumentException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (tag != null && startDate != null && endDate != null) {
            logService.generatePdfReportByDateRangeAndTag(startDate, endDate, tag, byteArrayOutputStream);
        } else if (tag != null) {
            logService.generatePdfReportByTag(tag, byteArrayOutputStream);
        } else if (startDate != null && endDate != null) {
            logService.generatePdfReportByDateRange(startDate, endDate, byteArrayOutputStream);
        } else {
            throw new IllegalArgumentException("At least one parameter (tag, startDate, endDate) must be provided");
        }

        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=log_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}