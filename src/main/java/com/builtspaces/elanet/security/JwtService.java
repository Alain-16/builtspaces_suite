package com.builtspaces.elanet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;                                                                                                                                                 
import io.jsonwebtoken.Jwts;                                                                                                                                                       
import io.jsonwebtoken.io.Decoders;                                                                                                                                                  
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;                                                                                                                           
import org.springframework.stereotype.Service;                                                                                                                                     
                                                                                                                                                                                     
import javax.crypto.SecretKey;
import java.util.Date;                                                                                                                                                               
import java.util.UUID; 
import com.builtspaces.elanet.users.Users;

@Service
public class JwtService {
	
	private final SecretKey signingKey;
	private final long accessTokenExpiry;
	private final long refreshTokenExpiry;
	
	public JwtService(@Value("${jwt.secret}") String secret,                                                                                                                                 
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,                                                                                                             
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.accessTokenExpiry = accessTokenExpiry;
		this.refreshTokenExpiry = refreshTokenExpiry;
	}
	
    public String generateAccessToken(Users user) {                                                                                                                                   
        return Jwts.builder()                                                                                                                                                      
                .subject(user.getId().toString())
                // orgId is embedded so TenantContextFilter can read it without a DB call
                .claim("orgId", user.getOrgId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                // type claim prevents refresh tokens from being used as access tokens                                                                                               
                .claim("type", "ACCESS")
                .issuedAt(new Date())                                                                                                                                                
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))                                                                                              
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Users user) {
        // Refresh tokens carry minimal claims — only enough to identify the user
        // and issue a new access token. No orgId, role, or email needed here.                                                                                                       
        return Jwts.builder()                                                                                                                                                        
                .subject(user.getId().toString())                                                                                                                                    
                .claim("type", "REFRESH")                                                                                                                                            
                .issuedAt(new Date())                                                                                                                                              
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(signingKey)                                                                                                                                                
                .compact();
    }                                                                                                                                                                                
                                                                                                                                                                                   
    // Package-private — only JwtAuthFilter and this class need direct claim access                                                                                                  
    Claims extractClaims(String token) {
        // Throws JwtException (and subclasses) for expired, malformed, or tampered tokens                                                                                           
        return Jwts.parser()                                                                                                                                                         
                .verifyWith(signingKey)
                .build()                                                                                                                                                             
                .parseSignedClaims(token)                                                                                                                                          
                .getPayload();
    }

    public UUID extractUserId(String token) {                                                                                                                                        
        return UUID.fromString(extractClaims(token).getSubject());
    }                                                                                                                                                                                
                                                                                                                                                                                   
    public String extractOrgId(String token) {
        return extractClaims(token).get("orgId", String.class);
    }

    public boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = extractClaims(token);
            // Both conditions must pass — right type AND not expired
            return expectedType.equals(claims.get("type", String.class))                                                                                                             
                    && claims.getExpiration().after(new Date());                                                                                                                     
        } catch (JwtException e) {                                                                                                                                                   
            // Covers: expired, malformed, wrong signature, unsupported format                                                                                                       
            return false;                                                                                                                                                            
        }
    } 

}
