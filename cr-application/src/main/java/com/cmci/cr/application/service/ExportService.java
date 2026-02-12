package com.cmci.cr.application.service;

import com.cmci.cr.application.dto.response.GroupStatisticsResponse;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service technique pour la g&eacute;n&eacute;ration de fichiers PDF et Excel (US4.4)
 */
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CMCI_HEADER = "CMCI - Communaut\u00e9 Missionnaire Chr\u00e9tienne Internationale";

    // ===== PDF - Stats Personnelles =====

    public byte[] exportPersonalStatsToPdf(PersonalStatisticsResponse stats, String userName,
                                           LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addPdfHeader(document, "Rapport Statistique Personnel");
            addPdfSubHeader(document, userName, startDate, endDate);

            PdfPTable table = createPdfTable(2);
            addTableHeader(table, "Indicateur", "Valeur");

            addTableRow(table, "Nombre total de CRs", String.valueOf(stats.getNombreTotalCRs()));
            addTableRow(table, "Taux de r\u00e9gularit\u00e9", formatPercent(stats.getTauxRegularite()));
            addTableRow(table, "RDQD compl\u00e9t\u00e9s", String.valueOf(stats.getRdqdCompletCount()));
            addTableRow(table, "Taux RDQD", formatPercent(stats.getTauxRDQD()));
            addTableRow(table, "Dur\u00e9e totale pri\u00e8re", stats.getDureeTotalePriere());
            addTableRow(table, "Dur\u00e9e moyenne pri\u00e8re", stats.getDureeMoyennePriere());
            addTableRow(table, "Chapitres lus (total)", String.valueOf(stats.getTotalChapitresLus()));
            addTableRow(table, "Chapitres/jour (moyenne)", String.format("%.1f", stats.getMoyenneChapitresParJour()));
            addTableRow(table, "Personnes \u00e9vang\u00e9lis\u00e9es", String.valueOf(stats.getTotalPersonnesEvangelisees()));
            addTableRow(table, "Confessions", String.valueOf(stats.getNombreConfessions()));
            addTableRow(table, "Je\u00fbnes", String.valueOf(stats.getNombreJeunes()));

            document.add(table);
            addPdfFooter(document);
            document.close();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la g\u00e9n\u00e9ration du PDF personnel", e);
        }
    }

    // ===== Excel - Stats Personnelles =====

    public byte[] exportPersonalStatsToExcel(PersonalStatisticsResponse stats, String userName,
                                             LocalDate startDate, LocalDate endDate) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Statistiques Personnelles");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            int rowIdx = 0;

            // Titre
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Rapport Statistique Personnel - " + userName);
            titleCell.setCellStyle(titleStyle);

            // P\u00e9riode
            org.apache.poi.ss.usermodel.Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("P\u00e9riode : " + formatDate(startDate) + " - " + formatDate(endDate));

            rowIdx++; // Ligne vide

            // En-t\u00eates
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell h1 = headerRow.createCell(0);
            h1.setCellValue("Indicateur");
            h1.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell h2 = headerRow.createCell(1);
            h2.setCellValue("Valeur");
            h2.setCellStyle(headerStyle);

            // Donn\u00e9es
            addExcelRow(sheet, rowIdx++, "Nombre total de CRs", String.valueOf(stats.getNombreTotalCRs()));
            addExcelRow(sheet, rowIdx++, "Taux de r\u00e9gularit\u00e9", formatPercent(stats.getTauxRegularite()));
            addExcelRow(sheet, rowIdx++, "RDQD compl\u00e9t\u00e9s", String.valueOf(stats.getRdqdCompletCount()));
            addExcelRow(sheet, rowIdx++, "Taux RDQD", formatPercent(stats.getTauxRDQD()));
            addExcelRow(sheet, rowIdx++, "Dur\u00e9e totale pri\u00e8re", stats.getDureeTotalePriere());
            addExcelRow(sheet, rowIdx++, "Dur\u00e9e moyenne pri\u00e8re", stats.getDureeMoyennePriere());
            addExcelRow(sheet, rowIdx++, "Chapitres lus (total)", String.valueOf(stats.getTotalChapitresLus()));
            addExcelRow(sheet, rowIdx++, "Chapitres/jour (moyenne)", String.format("%.1f", stats.getMoyenneChapitresParJour()));
            addExcelRow(sheet, rowIdx++, "Personnes \u00e9vang\u00e9lis\u00e9es", String.valueOf(stats.getTotalPersonnesEvangelisees()));
            addExcelRow(sheet, rowIdx++, "Confessions", String.valueOf(stats.getNombreConfessions()));
            addExcelRow(sheet, rowIdx++, "Je\u00fbnes", String.valueOf(stats.getNombreJeunes()));

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la g\u00e9n\u00e9ration de l'Excel personnel", e);
        }
    }

    // ===== PDF - Stats Groupe =====

    public byte[] exportGroupStatsToPdf(GroupStatisticsResponse stats, String groupName,
                                        LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addPdfHeader(document, "Rapport Statistique de Groupe");
            addPdfSubHeader(document, groupName, startDate, endDate);

            PdfPTable table = createPdfTable(2);
            addTableHeader(table, "Indicateur", "Valeur");

            addTableRow(table, "Nombre de membres", String.valueOf(stats.getNombreMembres()));
            addTableRow(table, "CRs soumis aujourd'hui", String.valueOf(stats.getNombreCRsAujourdhui()));
            addTableRow(table, "Taux de soumission du jour", formatPercent(stats.getTauxSoumissionJour()));
            addTableRow(table, "Total CRs sur la p\u00e9riode", String.valueOf(stats.getTotalCRsPeriode()));
            addTableRow(table, "Taux de r\u00e9gularit\u00e9 du groupe", formatPercent(stats.getTauxRegulariteGroupe()));
            addTableRow(table, "Dur\u00e9e totale pri\u00e8re", stats.getDureeTotalePriere());
            addTableRow(table, "Moyenne pri\u00e8re par membre", stats.getMoyennePriereParMembre());
            addTableRow(table, "Membres avec alerte (3j+)", String.valueOf(stats.getMembresAvecAlerte()));
            addTableRow(table, "Membres inactifs (7j+)", String.valueOf(stats.getMembresInactifs()));

            if (stats.getMeilleurDisciple() != null) {
                addTableRow(table, "Meilleur disciple", stats.getMeilleurDisciple());
                addTableRow(table, "Meilleur taux", formatPercent(stats.getMeilleurTaux()));
            }

            document.add(table);
            addPdfFooter(document);
            document.close();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la g\u00e9n\u00e9ration du PDF groupe", e);
        }
    }

    // ===== Excel - Stats Groupe =====

    public byte[] exportGroupStatsToExcel(GroupStatisticsResponse stats, String groupName,
                                          LocalDate startDate, LocalDate endDate) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Statistiques Groupe");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            int rowIdx = 0;

            // Titre
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Rapport Statistique de Groupe - " + groupName);
            titleCell.setCellStyle(titleStyle);

            // P\u00e9riode
            org.apache.poi.ss.usermodel.Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("P\u00e9riode : " + formatDate(startDate) + " - " + formatDate(endDate));

            rowIdx++; // Ligne vide

            // En-t\u00eates
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell h1 = headerRow.createCell(0);
            h1.setCellValue("Indicateur");
            h1.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell h2 = headerRow.createCell(1);
            h2.setCellValue("Valeur");
            h2.setCellStyle(headerStyle);

            // Donn\u00e9es
            addExcelRow(sheet, rowIdx++, "Nombre de membres", String.valueOf(stats.getNombreMembres()));
            addExcelRow(sheet, rowIdx++, "CRs soumis aujourd'hui", String.valueOf(stats.getNombreCRsAujourdhui()));
            addExcelRow(sheet, rowIdx++, "Taux de soumission du jour", formatPercent(stats.getTauxSoumissionJour()));
            addExcelRow(sheet, rowIdx++, "Total CRs sur la p\u00e9riode", String.valueOf(stats.getTotalCRsPeriode()));
            addExcelRow(sheet, rowIdx++, "Taux de r\u00e9gularit\u00e9 du groupe", formatPercent(stats.getTauxRegulariteGroupe()));
            addExcelRow(sheet, rowIdx++, "Dur\u00e9e totale pri\u00e8re", stats.getDureeTotalePriere());
            addExcelRow(sheet, rowIdx++, "Moyenne pri\u00e8re par membre", stats.getMoyennePriereParMembre());
            addExcelRow(sheet, rowIdx++, "Membres avec alerte (3j+)", String.valueOf(stats.getMembresAvecAlerte()));
            addExcelRow(sheet, rowIdx++, "Membres inactifs (7j+)", String.valueOf(stats.getMembresInactifs()));

            if (stats.getMeilleurDisciple() != null) {
                addExcelRow(sheet, rowIdx++, "Meilleur disciple", stats.getMeilleurDisciple());
                addExcelRow(sheet, rowIdx++, "Meilleur taux", formatPercent(stats.getMeilleurTaux()));
            }

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la g\u00e9n\u00e9ration de l'Excel groupe", e);
        }
    }

    // ===== Helpers PDF =====

    private void addPdfHeader(Document document, String title) {
        try {
            Font headerFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(99, 102, 241));
            Paragraph header = new Paragraph(CMCI_HEADER, headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(5f);
            document.add(header);

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20f);
            document.add(titlePara);
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur PDF header", e);
        }
    }

    private void addPdfSubHeader(Document document, String name, LocalDate startDate, LocalDate endDate) {
        try {
            Font subFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY);
            Paragraph sub = new Paragraph(
                    "Utilisateur : " + name + "  |  P\u00e9riode : " + formatDate(startDate) + " - " + formatDate(endDate),
                    subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20f);
            document.add(sub);
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur PDF sub-header", e);
        }
    }

    private PdfPTable createPdfTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(90f);
        table.setSpacingBefore(10f);
        try {
            table.setWidths(new float[]{3f, 2f});
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur PDF table widths", e);
        }
        return table;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(99, 102, 241));
            cell.setPadding(8f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(6f);
        labelCell.setBorderColor(new Color(229, 231, 235));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(6f);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setBorderColor(new Color(229, 231, 235));
        table.addCell(valueCell);
    }

    private void addPdfFooter(Document document) {
        try {
            Paragraph footer = new Paragraph(
                    "G\u00e9n\u00e9r\u00e9 le " + formatDate(LocalDate.now()) + " - CMCI CR Application",
                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30f);
            document.add(footer);
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur PDF footer", e);
        }
    }

    // ===== Helpers Excel =====

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private void addExcelRow(Sheet sheet, int rowIdx, String label, String value) {
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // ===== Helpers Communs =====

    private String formatPercent(Double value) {
        if (value == null) return "N/A";
        return String.format("%.1f%%", value);
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
