package com.builtspaces.elanet.common;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class TenantEntity {
	
	@Column(name="org_id",nullable=false,updatable=false)
	private UUID orgId;
	
	@PrePersist
	void assignTenant() {
		UUID tenant = TenantContext.getCurrentTenant();
		
		if(tenant == null) {
			throw new IllegalStateException("No tenant in context");
		}
		this.orgId = tenant;
	}
	
}
