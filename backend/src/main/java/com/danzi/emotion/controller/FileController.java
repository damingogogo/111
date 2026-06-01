package com.danzi.emotion.controller;

import com.danzi.emotion.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final MinioClient minioClient;
    private final MinioProperties properties;

    public FileController(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @GetMapping
    public void get(@RequestParam String object, HttpServletResponse response) throws Exception {
        if (object.contains("..") || object.startsWith("/") || object.startsWith("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid object name");
            return;
        }
        StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(object)
                .build());
        response.setContentType(stat.contentType() == null ? "application/octet-stream" : stat.contentType());
        response.setHeader("Cache-Control", "public, max-age=86400");
        try (InputStream input = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(object)
                .build())) {
            StreamUtils.copy(input, response.getOutputStream());
        }
    }
}
