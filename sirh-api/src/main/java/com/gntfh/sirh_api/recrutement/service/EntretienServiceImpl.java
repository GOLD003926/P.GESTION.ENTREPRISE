package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import com.gntfh.sirh_api.recrutement.entity.*;
import com.gntfh.sirh_api.recrutement.mapper.EntretienMapper;
import com.gntfh.sirh_api.recrutement.repository.*;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.exception.*;
import com.gntfh.sirh_api.recrutement.enums.TypeEntretien;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implémentation du service EntretienService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntretienServiceImpl implements EntretienService {
    private final EntretienRepository repository;
    private final CandidatureRepository candidatureRepository;
    private final EntretienIntervenantRepository intervenantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntretienMapper mapper;

    @Override
    public EntretienResponse obtenir(Long id, CurrentUser currentUser) {
        return response(get(id));
    }

    @Override
    public PageResponse<EntretienResponse> rechercher(Long candidatureId, StatutEntretien statut,
                                                     LocalDate du, LocalDate au, Pageable pageable, CurrentUser currentUser) {
        Specification<Entretien> s=(r,q,cb)->{var p=cb.conjunction();if(candidatureId!=null)p=cb.and(p,cb.equal(r.get("candidature").get("id"),candidatureId));if(statut!=null)p=cb.and(p,cb.equal(r.get("statut"),statut));if(du!=null)p=cb.and(p,cb.greaterThanOrEqualTo(r.get("dateHeure"),du.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));if(au!=null)p=cb.and(p,cb.lessThan(r.get("dateHeure"),au.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));return p;}; return PageResponse.from(repository.findAll(s,pageable),this::response);
    }

    @Override
    @Transactional
    public EntretienResponse creer(EntretienRequest request, CurrentUser currentUser) {
        Candidature c=candidatureRepository.findById(request.candidatureId()).orElseThrow(()->ResourceNotFoundException.of("Candidature",request.candidatureId())); Entretien e=repository.save(mapper.toEntity(request,c)); addIntervenants(e,request.intervenants()); return response(e);
    }

    @Override
    @Transactional
    public EntretienResponse modifier(Long id, EntretienUpdateRequest request, CurrentUser currentUser) {
        Entretien e=get(id); if(!e.getStatut().peutPasserA(StatutEntretien.REPORTE)) throw new InvalidStateTransitionException("Entretien",e.getStatut(),StatutEntretien.REPORTE); e.setStatut(StatutEntretien.REPORTE); mapper.updateEntity(request,e); e.setStatut(StatutEntretien.PLANIFIE); return response(e);
    }

    @Override
    @Transactional
    public EntretienResponse cloturer(Long id, CurrentUser currentUser) {
        return transition(id,StatutEntretien.REALISE,null);
    }

    @Override
    @Transactional
    public EntretienResponse annuler(Long id, AnnulationEntretienRequest request, CurrentUser currentUser) {
        return transition(id,StatutEntretien.ANNULE,request.motif());
    }

    @Override
    @Transactional
    public EntretienResponse marquerAbsent(Long id, CurrentUser currentUser) {
        return transition(id,StatutEntretien.CANDIDAT_ABSENT,null);
    }
    private Entretien get(Long id){return repository.findById(id).orElseThrow(()->ResourceNotFoundException.of("Entretien",id));}
    private EntretienResponse response(Entretien e){return mapper.toResponse(e,intervenantRepository.findByEntretienId(e.getId()),null);}
    private EntretienResponse transition(Long id,StatutEntretien target,String motif){Entretien e=get(id);if(!e.getStatut().peutPasserA(target))throw new InvalidStateTransitionException("Entretien",e.getStatut(),target);e.setStatut(target);e.setMotifAnnulation(motif);return response(e);}
    private void addIntervenants(Entretien e, java.util.List<com.gntfh.sirh_api.recrutement.dto.IntervenantRequest> list){for(var r:list){var u=utilisateurRepository.findById(r.utilisateurId()).orElseThrow(()->ResourceNotFoundException.of("Utilisateur",r.utilisateurId()));intervenantRepository.save(EntretienIntervenant.builder().entretien(e).utilisateur(u).roleIntervenant(r.roleIntervenant()).build());}}
}
