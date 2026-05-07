package com.builtspaces.elanet.common;

import com.builtspaces.elanet.common.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException,IOException{
		
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			
			if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
				
				TenantContext.setCurrentTenant(principal.getOrgId());
				
			}
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}

}
