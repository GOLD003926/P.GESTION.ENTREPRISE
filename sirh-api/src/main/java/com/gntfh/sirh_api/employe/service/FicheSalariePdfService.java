package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.employe.entity.Affectation;
import com.gntfh.sirh_api.employe.entity.Employe;
import org.openpdf.text.Document;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service pour la génération de la fiche salariale en PDF.
 */
@Service
@RequiredArgsConstructor
public class FicheSalariePdfService {

    /**
     * Génère un PDF contenant les informations de l'employé.
     */
    public byte[] generer(Employe employe, Optional<Affectation> affectationActuelle) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("FICHE SALARIALE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Matricule : " + employe.getMatricule()));
            document.add(new Paragraph("Nom : " + employe.getNom()));
            document.add(new Paragraph("Prénom : " + employe.getPrenom()));
            document.add(new Paragraph("Email pro : " + (employe.getEmailPro() != null ? employe.getEmailPro() : "N/A")));
            document.add(new Paragraph("Email personnel : " + (employe.getEmailPerso() != null ? employe.getEmailPerso() : "N/A")));
            document.add(new Paragraph("Téléphone : " + (employe.getTelephone() != null ? employe.getTelephone() : "N/A")));
            document.add(new Paragraph("Adresse : " + (employe.getAdresse() != null ? employe.getAdresse() : "N/A")));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Statut : " + employe.getStatut()));
            document.add(new Paragraph("Date d'embauche : " + (employe.getDateEmbauche() != null ? employe.getDateEmbauche().format(DateTimeFormatter.ISO_DATE) : "N/A")));
            document.add(new Paragraph(" "));

            if (affectationActuelle.isPresent()) {
                Affectation affectation = affectationActuelle.get();
                document.add(new Paragraph("Poste actuel : " + (affectation.getPoste() != null ? affectation.getPoste().getIntitule() : "N/A")));
                document.add(new Paragraph("Département : " + (affectation.getPoste() != null && affectation.getPoste().getDepartement() != null ? affectation.getPoste().getDepartement().getNom() : "N/A")));
                document.add(new Paragraph("Manager : " + (affectation.getManager() != null ? affectation.getManager().getNom() : "N/A")));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Date d'édition : " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)));

            document.close();
            return outputStream.toByteArray();
        } catch (org.openpdf.text.DocumentException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}
