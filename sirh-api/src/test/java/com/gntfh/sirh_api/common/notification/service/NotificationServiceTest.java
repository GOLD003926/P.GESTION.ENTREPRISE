package com.gntfh.sirh_api.common.notification.service;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.notification.entity.Notification;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.notification.repository.NotificationRepository;
import com.gntfh.sirh_api.common.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Utilisateur utilisateurActif;
    private Utilisateur utilisateurInactif;
    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        utilisateurActif = Utilisateur.builder()
                .email("test@sirh.local")
                .role(Role.RH)
                .actif(true)
                .build();

        utilisateurInactif = Utilisateur.builder()
                .email("inactif@sirh.local")
                .role(Role.RH)
                .actif(false)
                .build();

        currentUser = new CurrentUser(1L, "test@sirh.local", Role.RH);
    }

    @Test
    void notifier_destinataireExiste_notificationCreee() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurActif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifier(1L, TypeNotification.DEMANDE_A_VALIDER, "Titre", "Message", TypeEntite.DEMANDE_RECRUTEMENT, 1L);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void notifier_destinataireInexistant_exception() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.notifier(1L, TypeNotification.DEMANDE_A_VALIDER, "Titre", "Message", TypeEntite.DEMANDE_RECRUTEMENT, 1L)
        );
    }

    @Test
    void notifierParRole_comptesActifsNotifies_comptesInactifsIgnores() {
        when(utilisateurRepository.findByRoleAndActifTrue(Role.RH))
                .thenReturn(List.of(utilisateurActif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifierParRole(Role.RH, TypeNotification.DEMANDE_A_VALIDER, "Titre", "Message", TypeEntite.DEMANDE_RECRUTEMENT, 1L);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void notifierParRole_aucunCompteActif_aucuneNotification() {
        when(utilisateurRepository.findByRoleAndActifTrue(Role.RH))
                .thenReturn(List.of());

        notificationService.notifierParRole(Role.RH, TypeNotification.DEMANDE_A_VALIDER, "Titre", "Message", TypeEntite.DEMANDE_RECRUTEMENT, 1L);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void marquerLue_notificationAppartientUtilisateur_marqueeLue() {
        Notification notification = Notification.builder()
                .destinataire(utilisateurActif)
                .lue(false)
                .build();

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurActif));
        when(notificationRepository.findByIdAndDestinataire(1L, utilisateurActif))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = notificationService.marquerLue(1L, currentUser);

        assertTrue(response.lue());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void marquerLue_notificationNappartientPasUtilisateur_exception() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurActif));
        when(notificationRepository.findByIdAndDestinataire(1L, utilisateurActif))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.marquerLue(1L, currentUser)
        );
    }

    @Test
    void compter_retourneNombreNonLues() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurActif));
        when(notificationRepository.countByDestinataireAndLue(utilisateurActif, false))
                .thenReturn(5L);

        var response = notificationService.compter(currentUser);

        assertEquals(5L, response.nonLues());
    }

    @Test
    void toutMarquerLues_toutesNonLuesMarquees() {
        Notification notification1 = Notification.builder()
                .destinataire(utilisateurActif)
                .lue(false)
                .build();

        Notification notification2 = Notification.builder()
                .destinataire(utilisateurActif)
                .lue(false)
                .build();

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurActif));
        when(notificationRepository.findByDestinataireAndLue(utilisateurActif, false))
                .thenReturn(List.of(notification1, notification2));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.toutMarquerLues(currentUser);

        assertTrue(notification1.isLue());
        assertTrue(notification2.isLue());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}
