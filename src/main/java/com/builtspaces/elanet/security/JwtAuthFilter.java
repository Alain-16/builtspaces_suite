package com.builtspaces.elanet.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import com.builtspaces.elanet.security.JwtService;
import com.builtspaces.elanet.security.UserPrincipal;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	
	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}
	
	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
			)throws ServletException, IOException{
		
		String authHeader = request.getHeader("Authorization");
		
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = authHeader.substring(7);
		
		if(SecurityContextHolder.getContext().getAuthentication()== null && jwtService.isTokenValid(token,  "ACCESS") ) {
			
			String role = jwtService.extractClaims(token).get("role",String.class);
			
			UserPrincipal principal = new UserPrincipal(jwtService.extractUserId(token),UUID.fromString(jwtService.extractOrgId(token)));
			
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal,null,List.of(new SimpleGrantedAuthority("ROLE_" + role)));
			
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
			SecurityContextHolder.getContext().setAuthentication(authToken);
			
		}
		
		filterChain.doFilter(request, response);
		
	}
	

}
