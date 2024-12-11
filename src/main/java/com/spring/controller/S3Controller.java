package com.spring.controller;

import com.spring.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class S3Controller {

  private final S3Service s3Service;

  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  /**
   * DALL·E에서 생성된 이미지 URL을 받아서 S3에 업로드합니다.
   *
   * @param request DALL·E 이미지 URL이 포함된 요청
   * @return S3에 업로드된 이미지 URL
   */
  @PostMapping("/upload-dalle-image")
  public ResponseEntity<String> uploadDalleImage(@RequestBody Map<String, String> request) {
    try {
      String dalleImageUrl = request.get("url");

      // DALL·E URL에서 이미지 다운로드
      URL url = new URL(dalleImageUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      try (InputStream inputStream = connection.getInputStream()) {
        // S3에 업로드
        String s3Url = s3Service.uploadDalleImageToS3(inputStream, "generated-image-" + System.currentTimeMillis() + ".png");
        return ResponseEntity.ok(s3Url); // S3 URL 반환
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
    }
  }
}