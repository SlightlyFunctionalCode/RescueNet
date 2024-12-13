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

/**
 * Serviço para gerir os logs do sistema.
 *
 * <p>O serviço fornece as funcionalidades para guardar os logs, buscar logs  e gerar relatórios em PDF por diferentes
 * critérios (tag, intervalo de datas ou combinação de ambos). O objetivo é centralizar a gestão de logs e facilitar o
 * rastreamento de eventos importantes no sistema.</p>
 */
@Service
public class LogService {
    @Autowired
    private LogRepository logRepository;

    /**
     * Guarda um log no repositório.
     *
     * @param log Objeto de log a ser salvo.
     */
    public void saveLog(Log log) {
        logRepository.save(log);
    }

    /**
     * Gera um relatório em PDF de logs com base em tag e intervalo de datas.
     *
     * @param startDate Data inicial do intervalo.
     * @param endDate Data final do intervalo.
     * @param tag Tag para filtrar os logs.
     * @param byteArrayOutputStream Fluxo de saída onde o PDF será gerado.
     * @throws DocumentException Se houver erro durante a geração do PDF.
     */
    public void generatePdfReportByDateRangeAndTag(LocalDateTime startDate, LocalDateTime endDate, TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        List<Log> logs = logRepository.findByTagAndDateRange(tag, startDate, endDate);

        generatePdf(logs, byteArrayOutputStream);
    }

    /**
     * Gera um relatório em PDF de logs com base numa tag específica.
     *
     * @param tag Tag para filtrar os logs.
     * @param byteArrayOutputStream Fluxo de saída onde o PDF será gerado.
     * @throws DocumentException Se houver erro durante a geração do PDF.
     */
    public void generatePdfReportByTag(TagType tag, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        List<Log> logs = logRepository.findByTag(tag);

        generatePdf(logs, byteArrayOutputStream);
    }

    /**
     * Gera um relatório em PDF de logs com base num intervalo de datas.
     *
     * @param startDate Data inicial do intervalo.
     * @param endDate Data final do intervalo.
     * @param byteArrayOutputStream Fluxo de saída onde o PDF será gerado.
     * @throws DocumentException Se houver erro durante a geração do PDF.
     */
    public void generatePdfReportByDateRange(LocalDateTime startDate, LocalDateTime endDate, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        List<Log> logs = logRepository.findByDateRange(startDate, endDate);

        generatePdf(logs, byteArrayOutputStream);
    }

    /**
     * Método auxiliar para gerar o conteúdo do PDF a partir de uma lista de logs.
     *
     * @param logs Lista de logs a ser incluída no PDF.
     * @param byteArrayOutputStream Fluxo de saída onde o PDF será gerado.
     * @throws DocumentException Se houver erro durante a geração do PDF.
     */
    private void generatePdf(List<Log> logs, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument();
        pdfDoc.addWriter(writer);

        document.open();

        Paragraph title = new Paragraph("Log Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        float[] columnWidths = {1, 2, 3};
        table.setWidths(columnWidths);

        addTableHeader(table);

        for (Log log : logs) {
            addTableRow(table, log.getDateTime(), log.getTag(), log.getMessage());
        }

        document.add(table);

        document.close();
    }

    /**
     * Adiciona o cabeçalho à tabela do PDF.
     *
     * @param table Tabela onde o cabeçalho será adicionado.
     */
    private void addTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase("Tag", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Date", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Message", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
     * Adiciona uma linha à tabela do PDF.
     *
     * @param table Tabela onde a linha será adicionada.
     * @param date Data do log.
     * @param tag Tag do log.
     * @param message Mensagem do log.
     */
    private void addTableRow(PdfPTable table, String date, TagType tag, String message) {
        Color tagColor = getTagColor(tag);

        PdfPCell tagCell = new PdfPCell(new Phrase(tag.toString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        tagCell.setBackgroundColor(new BaseColor(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue()));
        tagCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(tagCell);

        table.addCell(new PdfPCell(new Phrase(date, FontFactory.getFont(FontFactory.HELVETICA, 10))));

        table.addCell(new PdfPCell(new Phrase(message, FontFactory.getFont(FontFactory.HELVETICA, 10))));
    }

    /**
     * Devolve a cor associada a uma tag.
     *
     * @param tag Tag do log.
     * @return Cor associada à tag.
     */
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