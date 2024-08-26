package com.prueba.dataservices.filter.advice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.IOException;
import java.io.InputStream;

public class HttpInputMessageDecorator implements HttpInputMessage {

    private final HttpInputMessage delegate;

    public HttpInputMessageDecorator(HttpInputMessage delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream getBody() throws IOException {
        return delegate.getBody();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
