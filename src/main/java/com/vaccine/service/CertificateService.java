package com.vaccine.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.vaccine.dto.CertificateFile;
import com.vaccine.entity.Appointment;
import com.vaccine.enums.AppointmentStatus;
import com.vaccine.enums.VaccineType;
import com.vaccine.exception.BusinessRuleException;
import com.vaccine.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final Color BLUE = new Color(0, 102, 153);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final AppointmentRepository appointmentRepo;

    @Transactional(readOnly = true)
    public CertificateFile generate(Long userId, VaccineType type) {
        // The rule: a certificate exists only for a COMPLETED vaccine
        Appointment a = appointmentRepo
                .findByUserIdAndVaccineTypeAndStatus(userId, type, AppointmentStatus.COMPLETED)
                .orElseThrow(() -> new BusinessRuleException(
                        "Certificate is available only after Vaccine " + type + " is completed"));

        byte[] pdf = render(a.getUser().getName(), a);
        return new CertificateFile("vaccine-" + type + "-certificate.pdf", pdf);
    }

    private byte[] render(String userName, Appointment a) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 60, 60, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // Decorative border
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(BLUE);
            cb.setLineWidth(3);
            cb.rectangle(25, 25, doc.getPageSize().getWidth() - 50, doc.getPageSize().getHeight() - 50);
            cb.stroke();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, BLUE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.DARK_GRAY);
            Font nameFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, Color.BLACK);
            Font bodyFont  = FontFactory.getFont(FontFactory.HELVETICA, 16, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);

            doc.add(line("COVID-19 VACCINATION CERTIFICATE", titleFont, 40));
            doc.add(line("This is to certify that", labelFont, 12));
            doc.add(line(userName, nameFont, 12));
            doc.add(line("has successfully received", labelFont, 12));
            doc.add(line("Vaccine " + a.getVaccineType(), nameFont, 20));
            doc.add(line("Date of vaccination: " + a.getCompletedDate().format(DATE_FMT), bodyFont, 8));
            doc.add(line("Certificate number: " + a.getCertificateNumber(), bodyFont, 30));
            doc.add(line("This certificate was generated electronically.", smallFont, 0));

            doc.close();   // must close BEFORE reading the bytes
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate certificate", e);
        }
    }

    private Paragraph line(String text, Font font, float spaceAfter) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(spaceAfter);
        return p;
    }
}