package com.projects.distributed_lovable.common_lib.security;



import com.projects.distributed_lovable.common_lib.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    @Value("${jwt.secret-key}")
    public String secretKey;

    public SecretKey getSecretKey()
    {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDto user)
    {
        return Jwts.builder()
                .subject(user.username())
                .claim("userId",user.id().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ 1000*60*10*30*10))
                .signWith(getSecretKey())
                .compact();
    }

    public JwtUserPrinciple verifyAccessToken(String token)
    {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.parseLong(claims.get("userId").toString());
        String username = claims.getSubject();
        return new JwtUserPrinciple(userId,username,null, new ArrayList<>());

    }

    public Long getCurrentUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if( authentication.getPrincipal() == null ||!(authentication.getPrincipal() instanceof JwtUserPrinciple))
        {
            throw new AuthenticationCredentialsNotFoundException("JWT TOKEN IS NOT FOUND!");
        }
        return ((JwtUserPrinciple) authentication.getPrincipal()).userId();
    }
}
