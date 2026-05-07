package com.builtspaces.elanet.common;

import java.util.UUID;

public final class TenantContext {
	
	private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
	
	private TenantContext() {}
	
	public static void setCurrentTenant(UUID orgId) {
		CURRENT_TENANT.set(orgId);
	}
	
	public static UUID getCurrentTenant() {
		return CURRENT_TENANT.get();
	}
	
	public static void clear() {
		CURRENT_TENANT.remove();
	}

}
