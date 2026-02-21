package com.ferrisys.common.entity.user;

import com.ferrisys.common.enums.DefaultUserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_status")
public class UserStatus implements Serializable {

    @Id
    @Column(name = "status_id", columnDefinition = "uuid")
    private UUID statusId;

    @Column(nullable = false)
    private String name;

    private String description;

    public UserStatus(UUID statusId) {
        this.statusId = statusId;
    }

    public static UserStatus fromCode(Integer code) {
        if (code == null || code == 1) {
            return new UserStatus(DefaultUserStatus.ACTIVE.getId());
        }
        return new UserStatus(DefaultUserStatus.INACTIVE.getId());
    }
}
