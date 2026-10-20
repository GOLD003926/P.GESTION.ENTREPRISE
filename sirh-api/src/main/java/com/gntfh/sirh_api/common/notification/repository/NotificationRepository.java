package com.gntfh.sirh_api.common.notification.repository;

import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.common.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByDestinataire(Utilisateur destinataire, Pageable pageable);

    Page<Notification> findByDestinataireAndLue(Utilisateur destinataire, Boolean lue, Pageable pageable);

    long countByDestinataireAndLue(Utilisateur destinataire, boolean lue);

    Optional<Notification> findByIdAndDestinataire(Long id, Utilisateur destinataire);

    List<Notification> findByDestinataireAndLue(Utilisateur destinataire, boolean lue);
}
