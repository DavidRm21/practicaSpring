package com.prueba.dataservices.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
//@Component
public class ReqResFilter /*implements Filter*/ {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest reader = new CachedBodyHttpServletRequest((HttpServletRequest) request);
        String requestBody = new BufferedReader(reader.getReader())
                .lines()
                .reduce("", String::concat);

        if (requestBody.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$")) {
            String reqDecoded = new String(Base64.getDecoder().decode(requestBody), StandardCharsets.UTF_8);
            JsonNode jsonNode = new ObjectMapper().readTree(reqDecoded);
            reader = new CachedBodyHttpServletRequest(reader, new ObjectMapper().writeValueAsString(jsonNode).getBytes(StandardCharsets.UTF_8));
            request = reader;
        }

        Base64ResponseWrapper responseWrapper = new Base64ResponseWrapper((HttpServletResponse) response);
        filterChain.doFilter(request, response);

        String capturedResponse = responseWrapper.getCapturedResponse();
        String base64EncodedResponse = Base64.getEncoder().encodeToString(capturedResponse.getBytes());
        response.setContentLength(base64EncodedResponse.length());
        response.getOutputStream().write(base64EncodedResponse.getBytes(StandardCharsets.UTF_8));
        log.info("fin del filtro");
    }
}
