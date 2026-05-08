package com.builtspaces.elanet.users;

import lombok.*;
import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
import com.builtspaces.elanet.users.UserRoles;

@Entity
@Table(name="users",indexes = {@Index(name="idx_user_org",columnList="org_id"),
							@Index(name="idx_user_email",columnList="email")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	@Column(name="org_id",nullable=false,updatable=false)
	private UUID OrgId;
	
	@Column(nullable=false,unique=true)
	private String email;
	
	@Column(nullable=false)
	private String passwordHash;
	
	@Column(nullable=false)
	private String fullName;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	@Builder.Default
	private UserRoles role = UserRoles.AGENT;
	
	@Column(nullable=false)
	@Builder.Default
	private boolean isActive = true;
	
	@Column(nullable=false,unique=false)
	private LocalDateTime createdAt;
	
	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
	

}
