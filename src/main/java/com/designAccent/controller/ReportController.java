package com.designAccent.controller;

import com.designAccent.entity.Expense;
import com.designAccent.entity.Project;
import com.designAccent.entity.ProjectCategory;
import com.designAccent.repository.ExpenseRepository;
import com.designAccent.repository.ProjectCategoryRepository;
import com.designAccent.repository.ProjectRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@CrossOrigin
public class ReportController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    @GetMapping("/api/reports/project/{projectId}")
    public ResponseEntity<byte[]> generateProjectReport(
            @PathVariable Long projectId)
            throws Exception {

        Project project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found"));

        List<Expense> expenses =
                expenseRepository.findByProjectId(
                        projectId);

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();
        Document document = new Document(
                PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        BaseColor navyColor  =
                new BaseColor(13, 27, 62);
        BaseColor goldColor  =
                new BaseColor(212, 175, 55);
        BaseColor lightGray  =
                new BaseColor(245, 246, 250);
        BaseColor greenColor =
                new BaseColor(46, 125, 50);
        BaseColor redColor   =
                new BaseColor(198, 40, 40);

        Font titleFont = new Font(
                Font.FontFamily.HELVETICA, 20,
                Font.BOLD, BaseColor.WHITE);
        Font headFont  = new Font(
                Font.FontFamily.HELVETICA, 13,
                Font.BOLD, navyColor);
        Font labelFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.NORMAL, BaseColor.GRAY);
        Font valueFont = new Font(
                Font.FontFamily.HELVETICA, 11,
                Font.BOLD, navyColor);
        Font greenFont = new Font(
                Font.FontFamily.HELVETICA, 11,
                Font.BOLD, greenColor);
        Font whiteFont = new Font(
                Font.FontFamily.HELVETICA, 10,
                Font.BOLD, BaseColor.WHITE);

        // ── HEADER ─────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(navyColor);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);
        Paragraph headerContent = new Paragraph();
        headerContent.add(new Chunk(
                "Design Accent\n",
                new Font(Font.FontFamily.HELVETICA,
                        11, Font.NORMAL, goldColor)));
        headerContent.add(new Chunk(
                "Project Expenses Report",
                titleFont));
        headerCell.addElement(headerContent);
        header.addCell(headerCell);
        document.add(header);
        document.add(new Paragraph(" "));

        // ── PROJECT INFO ───────────────────────
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);

        addInfoCell(infoTable, "Project Name",
                project.getProjectName(),
                navyColor, lightGray,
                labelFont, valueFont);
        addInfoCell(infoTable, "Client",
                project.getClientName() != null
                        ? project.getClientName()
                        : "-",
                navyColor, lightGray,
                labelFont, valueFont);
        addInfoCell(infoTable, "Type",
                project.getProjectType() != null
                        ? project.getProjectType()
                        : "-",
                navyColor, lightGray,
                labelFont, valueFont);
        addInfoCell(infoTable, "Status",
                project.getStatus() != null
                        ? project.getStatus()
                                .toUpperCase()
                        : "-",
                navyColor, lightGray,
                labelFont, valueFont);
        addInfoCell(infoTable, "Location",
                project.getLocation() != null
                        ? project.getLocation()
                        : "-",
                navyColor, lightGray,
                labelFont, valueFont);
        addInfoCell(infoTable, "Report Date",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy")),
                navyColor, lightGray,
                labelFont, valueFont);
        document.add(infoTable);
        document.add(new Paragraph(" "));

        // ── FINANCIAL SUMMARY ──────────────────
        Paragraph finTitle = new Paragraph(
                "Financial Summary", headFont);
        finTitle.setSpacingBefore(10);
        finTitle.setSpacingAfter(8);
        document.add(finTitle);

        PdfPTable finTable = new PdfPTable(3);
        finTable.setWidthPercentage(100);
        finTable.setSpacingAfter(10);

        addFinCell(finTable, "CONTRACT VALUE",
                formatAmount(
                        project.getTotalBudget()),
                navyColor, whiteFont, labelFont);
        addFinCell(finTable, "RECEIVED",
                formatAmount(
                        project.getTotalAmount()),
                new BaseColor(46, 125, 50),
                whiteFont, labelFont);

        BigDecimal vendorDues =
                project.getTotalBudget() != null
                && project.getTotalAmount() != null
                ? project.getTotalBudget()
                        .subtract(
                                project.getTotalAmount())
                : BigDecimal.ZERO;
        addFinCell(finTable, "VENDOR DUES",
                formatAmount(vendorDues),
                new BaseColor(198, 40, 40),
                whiteFont, labelFont);
        document.add(finTable);

        // payment breakdown
        PdfPTable payTable = new PdfPTable(3);
        payTable.setWidthPercentage(100);
        payTable.setSpacingAfter(16);

        addPayCell(payTable, "CASH",
                formatAmount(
                        project.getCashAmount()),
                new BaseColor(46, 125, 50),
                labelFont, greenFont);
        addPayCell(payTable, "ONLINE",
                formatAmount(
                        project.getOnlineAmount()),
                new BaseColor(25, 118, 210),
                labelFont, valueFont);
        addPayCell(payTable, "ACCOUNT",
                formatAmount(
                        project.getAccountAmount()),
                new BaseColor(230, 81, 0),
                labelFont, valueFont);
        document.add(payTable);

        // ── CATEGORY WISE EXPENSES ─────────────
        Paragraph catTitle = new Paragraph(
                "Category-wise Expense Details",
                headFont);
        catTitle.setSpacingBefore(10);
        catTitle.setSpacingAfter(8);
        document.add(catTitle);

        // only 2 columns — no total cost
        PdfPTable expTable = new PdfPTable(
                new float[]{3f, 2f});
        expTable.setWidthPercentage(100);

        String[] expHeaders = {
                "Category / Item", "Paid Amount"};
        for (String h : expHeaders) {
            PdfPCell hCell = new PdfPCell(
                    new Phrase(h, whiteFont));
            hCell.setBackgroundColor(navyColor);
            hCell.setPadding(8);
            hCell.setBorder(Rectangle.NO_BORDER);
            expTable.addCell(hCell);
        }

        List<ProjectCategory> parentCategories =
                categoryRepository
                        .findByParentIsNull();

        boolean alternate = false;
        for (ProjectCategory parent :
                parentCategories) {
            List<ProjectCategory> subcategories =
                    categoryRepository
                            .findByParentId(
                                    parent.getId());

            BigDecimal catTotal = BigDecimal.ZERO;
            boolean hasExpenses = false;

            java.util.List<String[]> subData =
                    new java.util.ArrayList<>();

            for (ProjectCategory sub :
                    subcategories) {
                Expense exp = findExpense(
                        expenses, sub.getId());
                if (exp != null) {
                    hasExpenses = true;
                    catTotal = catTotal.add(
                            exp.getAmount() != null
                                    ? exp.getAmount()
                                    : BigDecimal.ZERO);
                    // only name + paid amount
                    subData.add(new String[]{
                            "  • " + sub.getName(),
                            formatAmount(
                                    exp.getAmount())
                    });
                }
            }

            if (!hasExpenses) continue;

            BaseColor rowBg = alternate
                    ? new BaseColor(248, 249, 252)
                    : BaseColor.WHITE;

            // parent category row — 2 cells only
            PdfPCell catCell = new PdfPCell(
                    new Phrase(parent.getName(),
                            new Font(
                                    Font.FontFamily
                                            .HELVETICA,
                                    10, Font.BOLD,
                                    navyColor)));
            catCell.setBackgroundColor(
                    new BaseColor(237, 241, 250));
            catCell.setPadding(8);
            catCell.setBorder(Rectangle.BOTTOM);
            catCell.setBorderColor(
                    new BaseColor(220, 225, 240));
            expTable.addCell(catCell);

            PdfPCell cPaid = new PdfPCell(
                    new Phrase(
                            formatAmount(catTotal),
                            new Font(
                                    Font.FontFamily
                                            .HELVETICA,
                                    10, Font.BOLD,
                                    greenColor)));
            cPaid.setBackgroundColor(
                    new BaseColor(237, 241, 250));
            cPaid.setPadding(8);
            cPaid.setBorder(Rectangle.BOTTOM);
            cPaid.setBorderColor(
                    new BaseColor(220, 225, 240));
            expTable.addCell(cPaid);

            // sub rows — 2 cells only
            for (String[] row : subData) {
                for (int i = 0;
                        i < row.length; i++) {
                    Font f = i == 1
                            ? new Font(
                                    Font.FontFamily
                                            .HELVETICA,
                                    9, Font.NORMAL,
                                    greenColor)
                            : new Font(
                                    Font.FontFamily
                                            .HELVETICA,
                                    9, Font.NORMAL,
                                    navyColor);
                    PdfPCell cell = new PdfPCell(
                            new Phrase(row[i], f));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(6);
                    cell.setBorder(
                            Rectangle.BOTTOM);
                    cell.setBorderColor(
                            new BaseColor(
                                    240, 240, 240));
                    expTable.addCell(cell);
                }
                alternate = !alternate;
            }
        }

        document.add(expTable);

        // ── FOOTER ─────────────────────────────
        document.add(new Paragraph(" "));
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell footCell = new PdfPCell();
        footCell.setBackgroundColor(navyColor);
        footCell.setPadding(12);
        footCell.setBorder(Rectangle.NO_BORDER);
        footCell.addElement(new Paragraph(
                "Design Accent Architects & Interiors"
                + "  |  Generated on "
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy, hh:mm a")),
                new Font(Font.FontFamily.HELVETICA,
                        9, Font.NORMAL,
                        BaseColor.WHITE)));
        footer.addCell(footCell);
        document.add(footer);

        document.close();
        byte[] pdfBytes = baos.toByteArray();

        HttpHeaders httpHeaders =
                new HttpHeaders();
        httpHeaders.setContentType(
                MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData(
                "attachment",
                "project_report_"
                        + projectId + ".pdf");

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(pdfBytes);
    }

    @GetMapping("/api/reports/cashflow")
    public ResponseEntity<byte[]>
            generateCashFlowReport(
                    @RequestParam(required = false)
                    Integer month,
                    @RequestParam(required = false)
                    Integer year)
            throws Exception {

        List<Project> projects =
                projectRepository.findAll();

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();
        Document document = new Document(
                PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        BaseColor navyColor  =
                new BaseColor(13, 27, 62);
        BaseColor goldColor  =
                new BaseColor(212, 175, 55);
        BaseColor greenColor =
                new BaseColor(46, 125, 50);
        BaseColor redColor   =
                new BaseColor(198, 40, 40);

        Font titleFont = new Font(
                Font.FontFamily.HELVETICA, 20,
                Font.BOLD, BaseColor.WHITE);
        Font headFont  = new Font(
                Font.FontFamily.HELVETICA, 13,
                Font.BOLD, navyColor);
        Font labelFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.NORMAL, BaseColor.GRAY);
        Font valueFont = new Font(
                Font.FontFamily.HELVETICA, 11,
                Font.BOLD, navyColor);
        Font whiteFont = new Font(
                Font.FontFamily.HELVETICA, 10,
                Font.BOLD, BaseColor.WHITE);

        // header
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(navyColor);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);
        Paragraph hContent = new Paragraph();
        hContent.add(new Chunk("Design Accent\n",
                new Font(Font.FontFamily.HELVETICA,
                        11, Font.NORMAL, goldColor)));
        String period =
                month != null && year != null
                        ? getPeriodLabel(month, year)
                        : year != null
                                ? String.valueOf(year)
                                : "All Time";
        hContent.add(new Chunk(
                "Cash Flow Report — " + period,
                titleFont));
        headerCell.addElement(hContent);
        header.addCell(headerCell);
        document.add(header);
        document.add(new Paragraph(" "));

        // summary calculations
        BigDecimal totalInflow  = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;
        BigDecimal totalDues    = BigDecimal.ZERO;

        for (Project p : projects) {
            if (p.getTotalAmount() != null) {
                totalInflow = totalInflow.add(
                        p.getTotalAmount());
            }
            if (p.getTotalBudget() != null
                    && p.getTotalAmount() != null) {
                BigDecimal dues =
                        p.getTotalBudget().subtract(
                                p.getTotalAmount());
                if (dues.compareTo(
                        BigDecimal.ZERO) > 0) {
                    totalDues = totalDues.add(dues);
                }
            }
        }

        List<Expense> allExpenses =
                expenseRepository.findAll();
        for (Expense e : allExpenses) {
            if (e.getAmount() != null) {
                totalOutflow = totalOutflow.add(
                        e.getAmount());
            }
        }

        BigDecimal netPosition =
                totalInflow.subtract(totalOutflow);

        Paragraph sumTitle = new Paragraph(
                "Financial Overview", headFont);
        sumTitle.setSpacingBefore(10);
        sumTitle.setSpacingAfter(8);
        document.add(sumTitle);

        PdfPTable sumTable = new PdfPTable(2);
        sumTable.setWidthPercentage(100);
        sumTable.setSpacingAfter(16);

        addFinCell(sumTable, "TOTAL INFLOW",
                formatAmount(totalInflow),
                greenColor, whiteFont, labelFont);
        addFinCell(sumTable, "TOTAL OUTFLOW",
                formatAmount(totalOutflow),
                redColor, whiteFont, labelFont);
        addFinCell(sumTable, "VENDOR DUES",
                formatAmount(totalDues),
                new BaseColor(212, 175, 55),
                whiteFont, labelFont);
        addFinCell(sumTable, "NET POSITION",
                (netPosition.compareTo(
                        BigDecimal.ZERO) >= 0
                        ? "+" : "")
                + formatAmount(netPosition),
                navyColor, whiteFont, labelFont);
        document.add(sumTable);

        Paragraph projTitle = new Paragraph(
                "Project-wise Breakdown", headFont);
        projTitle.setSpacingBefore(10);
        projTitle.setSpacingAfter(8);
        document.add(projTitle);

        PdfPTable projTable = new PdfPTable(
                new float[]{3f, 1.5f, 1.5f,
                        1.5f, 1.5f});
        projTable.setWidthPercentage(100);

        String[] cols = {"Project", "Contract",
                "Received", "Dues", "Status"};
        for (String c : cols) {
            PdfPCell hc = new PdfPCell(
                    new Phrase(c, whiteFont));
            hc.setBackgroundColor(navyColor);
            hc.setPadding(8);
            hc.setBorder(Rectangle.NO_BORDER);
            projTable.addCell(hc);
        }

        boolean alt = false;
        for (Project p : projects) {
            BaseColor bg = alt
                    ? new BaseColor(248, 249, 252)
                    : BaseColor.WHITE;

            BigDecimal dues =
                    p.getTotalBudget() != null
                    && p.getTotalAmount() != null
                    ? p.getTotalBudget().subtract(
                            p.getTotalAmount())
                    : BigDecimal.ZERO;

            String[] row = {
                p.getProjectName() != null
                        ? p.getProjectName() : "-",
                formatAmount(p.getTotalBudget()),
                formatAmount(p.getTotalAmount()),
                formatAmount(dues),
                p.getStatus() != null
                        ? p.getStatus()
                                .toUpperCase()
                        : "-"
            };

            for (int i = 0;
                    i < row.length; i++) {
                Font f;
                if (i == 2) {
                    f = new Font(
                            Font.FontFamily.HELVETICA,
                            9, Font.NORMAL,
                            greenColor);
                } else if (i == 3) {
                    f = new Font(
                            Font.FontFamily.HELVETICA,
                            9, Font.NORMAL,
                            dues.compareTo(
                                    BigDecimal.ZERO)
                                    > 0
                                    ? redColor
                                    : navyColor);
                } else {
                    f = new Font(
                            Font.FontFamily.HELVETICA,
                            9, Font.NORMAL,
                            navyColor);
                }
                PdfPCell cell = new PdfPCell(
                        new Phrase(row[i], f));
                cell.setBackgroundColor(bg);
                cell.setPadding(7);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(
                        new BaseColor(
                                240, 240, 240));
                projTable.addCell(cell);
            }
            alt = !alt;
        }
        document.add(projTable);

        document.add(new Paragraph(" "));
        PdfPTable footTable = new PdfPTable(1);
        footTable.setWidthPercentage(100);
        PdfPCell fc = new PdfPCell();
        fc.setBackgroundColor(navyColor);
        fc.setPadding(12);
        fc.setBorder(Rectangle.NO_BORDER);
        fc.addElement(new Paragraph(
                "Design Accent Architects & Interiors"
                + "  |  Generated on "
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy, hh:mm a")),
                new Font(Font.FontFamily.HELVETICA,
                        9, Font.NORMAL,
                        BaseColor.WHITE)));
        footTable.addCell(fc);
        document.add(footTable);

        document.close();
        byte[] pdfBytes = baos.toByteArray();

        HttpHeaders httpHeaders =
                new HttpHeaders();
        httpHeaders.setContentType(
                MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData(
                "attachment",
                "cashflow_report_"
                        + period.replace(" ", "_")
                        + ".pdf");

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(pdfBytes);
    }

    private void addInfoCell(PdfPTable table,
            String label, String value,
            BaseColor navy, BaseColor bg,
            Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addFinCell(PdfPTable table,
            String label, String value,
            BaseColor bg, Font valueFont,
            Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(14);
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n",
                new Font(Font.FontFamily.HELVETICA,
                        9, Font.NORMAL,
                        new BaseColor(255, 255, 255,
                                180))));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addPayCell(PdfPTable table,
            String label, String value,
            BaseColor dotColor, Font labelFont,
            Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setPadding(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(
                new BaseColor(230, 230, 230));
        Paragraph p = new Paragraph();
        p.add(new Chunk("● " + label + "\n",
                new Font(Font.FontFamily.HELVETICA,
                        9, Font.NORMAL, dotColor)));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private Expense findExpense(
            List<Expense> expenses,
            Long categoryId) {
        return expenses.stream()
                .filter(e ->
                        e.getCategory() != null
                        && e.getCategory().getId()
                                .equals(categoryId))
                .findFirst()
                .orElse(null);
    }

    private String formatAmount(
            BigDecimal amount) {
        if (amount == null) return "Rs.0";
        double value = amount.doubleValue();
        if (value >= 10000000) {
            return String.format("Rs.%.1fCr",
                    value / 10000000);
        } else if (value >= 100000) {
            return String.format("Rs.%.1fL",
                    value / 100000);
        } else if (value >= 1000) {
            return String.format("Rs.%.1fK",
                    value / 1000);
        }
        return String.format("Rs.%.0f", value);
    }

    private String getPeriodLabel(
            int month, int year) {
        String[] months = {
            "", "January", "February", "March",
            "April", "May", "June", "July",
            "August", "September", "October",
            "November", "December"
        };
        return months[month] + " " + year;
    }
}