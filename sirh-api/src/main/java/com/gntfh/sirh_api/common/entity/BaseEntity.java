package com.gntfh.sirh_api.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Racine de toutes les entités : identifiant technique + version (verrouillage optimiste).
 * Le champ "version" évite que deux personnes écrasent mutuellement leurs modifications
 * (ex. deux validateurs sur la même demande).
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> thatClass = o instanceof HibernateProxy proxyO
                ? proxyO.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisClass = this instanceof HibernateProxy proxyThis
                ? proxyThis.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisClass != thatClass) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
