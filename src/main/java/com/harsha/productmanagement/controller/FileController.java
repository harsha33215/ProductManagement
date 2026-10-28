
package com.harsha.productmanagement.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.harsha.productmanagement.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        String key = s3Service.uploadFile(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "File uploaded successfully",
                        "key", key,
                        "size", file.getSize()
                ));
    }
}
