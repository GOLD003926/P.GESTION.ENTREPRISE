package com.gntfh.sirh_api.auth.service;

import com.gntfh.sirh_api.auth.dto.ResetPasswordRequest;
import com.gntfh.sirh_api.auth.dto.UtilisateurRequest;
import com.gntfh.sirh_api.auth.dto.UtilisateurResponse;
import com.gntfh.sirh_api.auth.dto.UtilisateurUpdateRequest;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.mapper.UtilisateurMapper;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

/**
 * Gestion des comptes utilisateurs. Toutes les règles de gestion du module sont ici :
 *  - l'email est unique (sans tenir compte de la casse) et immuable
 *  - un administrateur ne peut ni se désactiver ni changer son propre rôle
 *    (il reste ainsi toujours au moins un administrateur actif)
 *  - pas de suppression : on désactive
 *
 * Convention : les méthodes d'écriture sont @Transactional ; les entités chargées dans la
 * transaction sont modifiées directement (Hibernate détecte les changements, pas de save()).
 */
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    /** Champs sur lesquels le client a le droit de trier (jamais motDePasse). */
    private static final Set<String> TRIS_AUTORISES =
            Set.of("id", "email", "nom", "prenom", "role", "actif", "createdAt");

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurMapper utilisateurMapper;
    private final EmployeRepository employeRepository;

    @Transactional(readOnly = true)
    public PageResponse<UtilisateurResponse> rechercher(String q, Role role, Boolean actif, Pageable pageable) {
        verifierTri(pageable);

        // Construction d'une recherche à filtres facultatifs : chaque filtre fourni s'ajoute en "ET".
        Specification<Utilisateur> spec = (root, query, cb) -> cb.conjunction();

        if (q != null && !q.isBlank()) {
            String motif = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), motif),
                    cb.like(cb.lower(root.get("nom")), motif),
                    cb.like(cb.lower(root.get("prenom")), motif)));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (actif != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), actif));
        }

        return PageResponse.from(utilisateurRepository.findAll(spec, pageable), utilisateurMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse obtenir(Long id) {
        return utilisateurMapper.toResponse(trouver(id));
    }

    @Transactional
    public UtilisateurResponse creer(UtilisateurRequest request) {
        String email = request.email().trim();
        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw DuplicateResourceException.of("Un utilisateur", "l'email", email);
        }
        if (request.employeId() != null && !employeRepository.existsById(request.employeId())) {
            throw ResourceNotFoundException.of("Employe", request.employeId());
        }
        Utilisateur utilisateur = utilisateurMapper.toEntity(request, passwordEncoder.encode(request.motDePasse()));
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurResponse modifier(Long id, UtilisateurUpdateRequest request, String acteurEmail) {
        Utilisateur utilisateur = trouver(id);
        if (estActeur(utilisateur, acteurEmail) && utilisateur.getRole() != request.role()) {
            throw new BusinessRuleException("Vous ne pouvez pas modifier votre propre rôle.");
        }
        if (request.employeId() != null && !employeRepository.existsById(request.employeId())) {
            throw ResourceNotFoundException.of("Employe", request.employeId());
        }
        utilisateurMapper.updateEntity(request, utilisateur);
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse activer(Long id) {
        Utilisateur utilisateur = trouver(id);
        utilisateur.setActif(true);
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse desactiver(Long id, String acteurEmail) {
        Utilisateur utilisateur = trouver(id);
        if (estActeur(utilisateur, acteurEmail)) {
            throw new BusinessRuleException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        utilisateur.setActif(false);
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, ResetPasswordRequest request) {
        Utilisateur utilisateur = trouver(id);
        utilisateur.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
    }

    // ---------------------------------------------------------------------

    private Utilisateur trouver(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }

    private boolean estActeur(Utilisateur utilisateur, String acteurEmail) {
        return utilisateur.getEmail().equalsIgnoreCase(acteurEmail);
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!TRIS_AUTORISES.contains(order.getProperty())) {
                throw new BusinessRuleException("Tri non autorisé sur le champ « %s ».".formatted(order.getProperty()));
            }
        });
    }
}
