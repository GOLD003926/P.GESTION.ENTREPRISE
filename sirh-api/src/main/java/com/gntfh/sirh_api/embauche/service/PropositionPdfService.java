package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PropositionPdfService {

    public byte[] generer(PropositionEmbauche proposition) {
        Candidat candidat = proposition.getCandidature().getCandidat();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("PROPOSITION D'EMBAUCHE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Candidat : " + candidat.getPrenom() + " " + candidat.getNom()));
            document.add(new Paragraph("Email : " + candidat.getEmail()));
            document.add(new Paragraph("Poste : " + proposition.getPoste().getIntitule()));
            document.add(new Paragraph("Type de contrat : " + proposition.getTypeContratPropose()));
            document.add(new Paragraph("Salaire proposé : " + proposition.getSalairePropose()
                    + " " + proposition.getDevise()));
            document.add(new Paragraph("Horaires de travail : " + proposition.getHorairesTravail()));
            document.add(new Paragraph("Lieu de travail : " + proposition.getLieuTravail()));
            document.add(new Paragraph("Date de début prévue : "
                    + proposition.getDateDebutPrevue().format(DateTimeFormatter.ISO_DATE)));
            document.add(new Paragraph("Durée : " + (proposition.getDureeMois() == null
                    ? "Indéterminée" : proposition.getDureeMois() + " mois")));
            document.add(new Paragraph("Date limite de réponse : "
                    + proposition.getDateLimiteReponse().format(DateTimeFormatter.ISO_DATE)));
            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException("La génération du PDF de proposition a échoué", exception);
        }
    }
}
