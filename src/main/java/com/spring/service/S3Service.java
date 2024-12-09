package com.spring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

  private final S3Client s3Client;

  @Value("${aws.bucketName}")
  private String bucketName;

  @Value("${aws.region}")
  private String region;

  public S3Service(@Value("${aws.accessKey}") String accessKey, @Value("${aws.secretKey}") String secretKey, @Value("${aws.region}") String region) {
    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  public String uploadDalleImageToS3(InputStream inputStream, String fileName) {
    try {
      // 이미지 검증 (이미지로 읽을 수 있는지 확인)
      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) {
        throw new RuntimeException("The downloaded file is not a valid image.");
      }

      // InputStream을 ByteArray로 변환하여 업로드
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      ImageIO.write(image, "png", buffer);
      byte[] byteArray = buffer.toByteArray();

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .contentType("image/png") // 이미지의 Content-Type 설정
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(byteArray));
      String s3Url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
      System.out.println("Image uploaded successfully to S3: " + s3Url);
      return s3Url;
    } catch (IOException e) {
      System.err.println("Error uploading image to S3: " + e.getMessage());
      throw new RuntimeException("Error uploading image to S3: " + e.getMessage(), e);
    }
  }

}
