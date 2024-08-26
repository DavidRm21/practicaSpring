package com.prueba.dataservices.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Interceptor implements HandlerInterceptor {

    private Base64ResponseWrapper responseWrapper;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpServletRequest reader = new CachedBodyHttpServletRequest(request);
        String requestBody = new BufferedReader(reader.getReader())
                .lines()
                .reduce("", String::concat);

        if (requestBody.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$")) {
            String reqDecoded = new String(Base64.getDecoder().decode(requestBody), StandardCharsets.UTF_8);
            JsonNode jsonNode = new ObjectMapper().readTree(reqDecoded);
            reader = new CachedBodyHttpServletRequest(reader, new ObjectMapper().writeValueAsString(jsonNode).getBytes(StandardCharsets.UTF_8));
            request = reader;
        }

        responseWrapper = new Base64ResponseWrapper(response);


        /*log.info("PRE HANDLER-----");
        // Obtener la URL del request
        String requestURI = request.getRequestURI();
        log.info("Hanler_ {}", handler.toString());

        // Definir el patrón para extraer los valores de issuerId y consumerId
        String pattern = "/cat/([^/]+)/x/([^/]+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(requestURI);

        if (matcher.find()) {
            // Extraer issuerId y consumerId
            String issuerId = matcher.group(1);
            String consumerId = matcher.group(2);

            // Ahora puedes usar issuerId y consumerId como desees
            log.info("Issuer ID: " + issuerId);
            log.info("Consumer ID: " + consumerId);
            log.info("accountId: {}", request.getHeader("accountId"));
            log.info("clientId: {}", request.getHeader("client_id"));
            log.info("remoteAddress: {}", request.getRemoteAddr());
            log.info("Path: {}", request.getMethod());
        }*/
        return HandlerInterceptor.super.preHandle(request, responseWrapper, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("POST HANDLER-----");
        String capturedResponse = responseWrapper.getCapturedResponse();
        String base64EncodedResponse = Base64.getEncoder().encodeToString(capturedResponse.getBytes());
        response.setContentLength(base64EncodedResponse.length());
        response.getOutputStream().write(base64EncodedResponse.getBytes(StandardCharsets.UTF_8));
        log.info("fin del filtro");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
