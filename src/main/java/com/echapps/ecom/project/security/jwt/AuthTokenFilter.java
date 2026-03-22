package com.echapps.ecom.project.security.jwt;

import com.echapps.ecom.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            logger.debug("Attempting to parse JWT from request");
            String jwt = parseJwt(request);
                if (jwt != null && jwtUtils.validateToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwt(jwt);
                    logger.debug("JWT is valid. Username extracted: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("UserDetails loaded for username: {}", username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.debug("Authentication object created for user: {}", username);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set in SecurityContext for user: {}", userDetails.getAuthorities());
                } else {
                    logger.debug("No valid JWT token found in the request");
                }
    } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {

        // Get JWT from cookie first
        String jwtFromCookie = jwtUtils.getJwtFromCookie(request);
        logger.debug("Parsed JWT Cookie: {}", jwtFromCookie);
        if (jwtFromCookie != null) {
            return jwtFromCookie;
        }

        // If no JWT in cookie, try to get it from header
        String jwtFromHeader = jwtUtils.getJwtFromRequestHeader(request);
        logger.debug("Parsed JWT Header: {}", jwtFromHeader);
        return jwtFromHeader;
    }
}
