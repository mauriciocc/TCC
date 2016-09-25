package com.simple.app.sync.report.web;

import com.simple.app.sync.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api/report")
public class ReportResource {

    private final ReportService reportService;

    @Autowired
    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String render() {
        return render(
                reportService.findTodos(),
                reportService.findImage()
        );
    }

    private String render(String json, byte[] image) {
        return "<pre>" + json + "</pre>" +
                "<img src='data:image/png;" + Base64.getEncoder().encodeToString(image) + "'/>";
    }
}