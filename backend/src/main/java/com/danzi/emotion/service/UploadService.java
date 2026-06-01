package com.danzi.emotion.service;

import com.danzi.emotion.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {
    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final JdbcTemplate jdbcTemplate;

    public UploadService(MinioClient minioClient, MinioProperties properties, JdbcTemplate jdbcTemplate) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> upload(MultipartFile file, String bizType) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
        }
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String suffix = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
        String objectName = "emotion-ai/" + LocalDate.now() + "/" + UUID.randomUUID() + suffix;
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());
        String url = properties.getPublicBaseUrl() + "/" + objectName;
        String proxyUrl = "/api/files?object=" + objectName;
        jdbcTemplate.update("""
                insert into upload_files(file_name, object_name, url, content_type, size_bytes, biz_type)
                values (?, ?, ?, ?, ?, ?)
                """, originalName, objectName, url, contentType, file.getSize(), bizType);
        return Map.of(
                "fileName", originalName,
                "objectName", objectName,
                "url", url,
                "proxyUrl", proxyUrl,
                "contentType", contentType,
                "size", file.getSize()
        );
    }
}
