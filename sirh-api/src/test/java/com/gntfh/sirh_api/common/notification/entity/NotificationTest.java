package com.gntfh.sirh_api.common.notification.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Une notification est créée NON LUE par défaut. */
class NotificationTest {

    @Test
    void builder_sansValeur_estNonLue() {
        assertFalse(Notification.builder().build().isLue());
    }

    @Test
    void builder_lueFaux_estNonLue() {
        assertFalse(Notification.builder().lue(false).build().isLue());
    }

    @Test
    void builder_lueVrai_estLue() {
        assertTrue(Notification.builder().lue(true).build().isLue());
    }
}
