package com.app.coworking.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    @Value("${logging.file.name}")
    private String logFilePath;

    @Operation(summary = "Get logs by date",
            description = "Retrieves and saves logs filtered by a specific date")
    @GetMapping("/by-date")
    public ResponseEntity<Resource> getLogsByDate(
            @RequestParam @NotNull(message = "Date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        String filterDate = date.toString() + "T";  // 2025-09-16T
        List<String> filteredLogs = Files.readAllLines(Paths.get(logFilePath))
                .stream()
                .filter(line -> line.contains(filterDate))
                .toList();

        if (filteredLogs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path dailyLogsDir = Paths.get("daily-logs");
        if (!Files.exists(dailyLogsDir)) {
            Files.createDirectory(dailyLogsDir);
        }

        String dailyLogFileName = "logs-" + date + ".log";
        Path dailyLogPath = dailyLogsDir.resolve(dailyLogFileName);

        Files.write(dailyLogPath, filteredLogs);

        Resource resource = new UrlResource(dailyLogPath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}