package com.bank.cashdesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class APIKeyFilter extends OncePerRequestFilter {

    @Autowired
    private AppPropertiesConfig propertiesConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKeyHeader = request.getHeader(propertiesConfig.getRequestHeader());

        if (!StringUtils.hasText(apiKeyHeader) || !apiKeyHeader.equals(propertiesConfig.getApiKey())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: Invalid API key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
