package com.spring.controller;

import com.spring.model.UserLocation;
import com.spring.model.schedule.LocationRequest;
import com.spring.service.weather.UserLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
public class LocationController {

  @Autowired
  private UserLocationService userLocationService;

  // 위치 정보 저장 API
  @PostMapping
  public ResponseEntity<?> saveUserLocation(@RequestBody LocationRequest locationRequest) {
    try {
      userLocationService.saveLocation(locationRequest.getUserId(), locationRequest.getLatitude(), locationRequest.getLongitude());
      return ResponseEntity.ok("Location saved successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving location - duplicate");
    }
  }

//  // 위치 정보 조회 API
//  @GetMapping("/{userId}")
//  public ResponseEntity<UserLocation> getUserLocation(@PathVariable String userId) {
//    return userLocationService.getLocationByUserId(userId)
//        .map(ResponseEntity::ok)
//        .orElse(ResponseEntity.notFound().build());
//  }
}
