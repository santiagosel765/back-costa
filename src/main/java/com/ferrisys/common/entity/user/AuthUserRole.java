package com.ferrisys.common.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_user_role")
public class AuthUserRole implements Serializable {

	@Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(columnDefinition = "uuid", updatable = false)
        private UUID id;

	@ManyToOne
	@JoinColumn(name = "auth_user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "auth_role_id", nullable = false)
	private Role role;

	@Builder.Default
	private Integer status = 1;
}