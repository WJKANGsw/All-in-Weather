package com.spring.service.weather;

import com.spring.model.UserLocation;
import com.spring.repository.UserLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLocationService {

  @Autowired
  private UserLocationRepository userLocationRepository;

  // 위치 정보 저장 메서드
  public void saveLocation(String userId, double latitude, double longitude) {
    UserLocation userLocation = new UserLocation();
    userLocation.setUserId(userId);
    userLocation.setLatitude(latitude);
    userLocation.setLongitude(longitude);
    userLocationRepository.save(userLocation);
  }

//  // 위치 정보 조회 메서드
//  public Optional<UserLocation> getLocationByUserId(String userId) {
//    return userLocationRepository.findByUserId(userId);
//  }
}

