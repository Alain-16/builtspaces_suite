package com.builtspaces.elanet.organization;

import lombok.*;
import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
import com.builtspaces.elanet.organization.PlanTier;


@Entity
@Table(name="organization")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
	
	@Id
	@GeneratedValue(strategy= GenerationType.UUID)
	private UUID id;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false,unique=true)
	private String slug;
	
	private String logoUrl;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PlanTier planTier = PlanTier.STARTER;
	
	@Column(nullable=false,updatable=false)
	private LocalDateTime createdAt;
	
	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
	

}
