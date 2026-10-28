package com.harsha.productmanagement.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {
    private final S3Client s3Client;
    private  final String bucketName;

    public S3Service(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucketName) {

        System.out.println("S3Service constructor called");

        this.s3Client = s3Client;
        this.bucketName = bucketName;

        System.out.println("Configured S3 bucket: [" + this.bucketName + "]");
    }
    public  String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a non empty file"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }
        String safeFilename = originalFilename.replace('\\', '/');
        String key = "uploads/" + UUID.randomUUID() + "-" + safeFilename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType() == null
                        ? "application/octet-stream" : file.getContentType())
                .build();
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(request,
                    RequestBody.fromInputStream(
                            inputStream,
                            file.getSize()
                    )
            );
        }
        return key;

    }

}
