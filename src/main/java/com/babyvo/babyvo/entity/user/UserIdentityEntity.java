package com.babyvo.babyvo.entity.user;

import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Google/Apple/Email gibi sağlayıcı kimlikleri.
 */

@Entity
@Table(
        name = "user_identities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_identity_provider_subject", columnNames = {"provider", "provider_subject"})
        },
        indexes = {
                @Index(name = "idx_user_identities_user_id", columnList = "user_id"),
                @Index(name = "idx_user_identities_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_identity_user"))
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IdentityProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private Boolean emailVerified = false;
}
