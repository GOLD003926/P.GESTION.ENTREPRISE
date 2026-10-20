package com.gntfh.sirh_api.common.document.service;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.config.SirhProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final SirhProperties sirhProperties;

    public String sauvegarder(byte[] contenu, TypeEntite type, Long entiteId, String nomFichier) throws IOException {
        String nomNettoye = nettoyerNomFichier(nomFichier);
        String nomUnique = UUID.randomUUID() + "-" + nomNettoye;

        Path dossierStockage = getDossierStockage(type, entiteId);
        if (!Files.exists(dossierStockage)) {
            Files.createDirectories(dossierStockage);
        }

        Path cheminComplet = dossierStockage.resolve(nomUnique);
        Files.write(cheminComplet, contenu);

        return obtenirCheminRelatif(type, entiteId, nomUnique);
    }

    public Resource charger(String cheminRelatif) throws IOException {
        Path cheminComplet = normaliserChemin(cheminRelatif);
        if (!cheminComplet.startsWith(getRacineStockage())) {
            throw new SecurityException("Tentative de sortie du dossier de stockage");
        }

        if (!Files.exists(cheminComplet) || !Files.isReadable(cheminComplet)) {
            throw new IOException("Fichier introuvable ou illisible : " + cheminRelatif);
        }

        return new UrlResource(cheminComplet.toUri());
    }

    private String nettoyerNomFichier(String nomFichier) {
        return nomFichier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private Path getDossierStockage(TypeEntite type, Long entiteId) {
        return getRacineStockage()
                .resolve(type.name())
                .resolve(String.valueOf(entiteId));
    }

    private Path getRacineStockage() {
        return Paths.get(sirhProperties.storage().location()).toAbsolutePath().normalize();
    }

    private String obtenirCheminRelatif(TypeEntite type, Long entiteId, String nomUnique) {
        return String.format("%s/%d/%s", type.name(), entiteId, nomUnique);
    }

    private Path normaliserChemin(String cheminRelatif) {
        Path chemin = getRacineStockage().resolve(cheminRelatif).normalize();
        if (chemin.toString().contains("..")) {
            throw new SecurityException("Chemin invalide : contient '..'");
        }
        return chemin;
    }
}
