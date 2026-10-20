package com.gntfh.sirh_api.common.notification.service;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.notification.dto.NotificationCompteurResponse;
import com.gntfh.sirh_api.common.notification.dto.NotificationResponse;
import com.gntfh.sirh_api.common.notification.entity.Notification;
import com.gntfh.sirh_api.common.notification.enums.CanalNotification;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.notification.repository.NotificationRepository;
import com.gntfh.sirh_api.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public void notifier(Long destinataireId, TypeNotification type, String titre, String message, TypeEntite entiteType, Long entiteId) {
        Utilisateur destinataire = utilisateurRepository.findById(destinataireId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", destinataireId));

        Notification notification = Notification.builder()
                .destinataire(destinataire)
                .type(type)
                .canal(CanalNotification.INTERNE)
                .titre(titre)
                .message(message)
                .entiteType(entiteType)
                .entiteId(entiteId)
                .lue(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void notifierParRole(Role role, TypeNotification type, String titre, String message, TypeEntite entiteType, Long entiteId) {
        List<Utilisateur> destinataires = utilisateurRepository.findByRoleAndActifTrue(role);

        for (Utilisateur destinataire : destinataires) {
            Notification notification = Notification.builder()
                    .destinataire(destinataire)
                    .type(type)
                    .canal(CanalNotification.INTERNE)
                    .titre(titre)
                    .message(message)
                    .entiteType(entiteType)
                    .entiteId(entiteId)
                    .lue(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> mesNotifications(CurrentUser currentUser, Boolean lue, Pageable pageable) {
        Utilisateur utilisateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));

        Page<Notification> page;
        if (lue == null) {
            page = notificationRepository.findByDestinataire(utilisateur, pageable);
        } else {
            page = notificationRepository.findByDestinataireAndLue(utilisateur, lue, pageable);
        }

        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public NotificationCompteurResponse compter(CurrentUser currentUser) {
        Utilisateur utilisateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));

        long nonLues = notificationRepository.countByDestinataireAndLue(utilisateur, false);

        return new NotificationCompteurResponse(nonLues);
    }

    @Transactional
    public NotificationResponse marquerLue(Long id, CurrentUser currentUser) {
        Utilisateur utilisateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));

        Notification notification = notificationRepository.findByIdAndDestinataire(id, utilisateur)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));

        notification.setLue(true);
        notification.setDateLecture(Instant.now());

        notificationRepository.save(notification);

        return toResponse(notification);
    }

    @Transactional
    public void toutMarquerLues(CurrentUser currentUser) {
        Utilisateur utilisateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));

        List<Notification> notifications = notificationRepository.findByDestinataireAndLue(utilisateur, false);

        for (Notification notification : notifications) {
            notification.setLue(true);
            notification.setDateLecture(Instant.now());
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitre(),
                notification.getMessage(),
                notification.getEntiteType(),
                notification.getEntiteId(),
                notification.isLue(),
                notification.getDateLecture(),
                notification.getCreatedAt()
        );
    }
}
