package com.simple.app.sync.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ReportService {

    private final RestTemplate httpClient;
    private final String reportUrl;
    private final String raytracerUrl;

    @Autowired
    public ReportService(RestTemplate httpClient,
                         @Value("${report.todo.url}") String reportUrl,
                         @Value("${report.raytracer.url}") String raytracerUrl) {
        this.httpClient = httpClient;
        this.reportUrl = reportUrl;
        this.raytracerUrl = raytracerUrl;
    }

    public String findTodos() {
        final String url = UriComponentsBuilder.fromHttpUrl(reportUrl).path("/api/todos").toUriString();
        return httpClient.getForEntity(url, String.class).getBody();
    }

    public byte[] findImage() {
        final String url = UriComponentsBuilder.fromHttpUrl(raytracerUrl).path("/api/render").toUriString();
        return httpClient.getForEntity(url, byte[].class).getBody();
    }
}
