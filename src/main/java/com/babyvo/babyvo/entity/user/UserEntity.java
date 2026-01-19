package com.babyvo.babyvo.entity.user;

import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kullanıcının “asıl” kaydı. Login sağlayıcıları ayrı tabloda.
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {
    @Column(length = 120)
    private String displayName;

    @Column(length = 255)
    private String primaryEmail;
}
