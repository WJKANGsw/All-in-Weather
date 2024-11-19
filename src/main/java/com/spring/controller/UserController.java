package com.spring.controller;

import com.spring.model.*;
import com.spring.model.dto.request.auth.CheckCertificationRequestDto;
import com.spring.model.dto.request.auth.EmailCertificationRequestDto;
import com.spring.model.dto.request.auth.IdCheckRequestDto;
import com.spring.model.social_dto.CustomOAuth2User;
import com.spring.model.social_dto.SocialUserDTO;
import com.spring.repository.AllUserRepository;
import com.spring.security.JwtTokenProvider;
import com.spring.service.AllUserService;
import com.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AllUserService allUserService;
    private final JwtTokenProvider jwtTokenProvider;

//    private final SocialUserRepository socialUserRepository;

    private final AllUserRepository allUserRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger 추가


    @PostMapping("/check-userId")
    public ResponseEntity<Boolean> checkUserId(@RequestBody IdCheckRequestDto requestDto) {
        boolean exists = allUserService.userIdExists(requestDto.getUserId());
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestBody EmailCertificationRequestDto requestDto) {
        boolean exists = allUserService.emailExists(requestDto.getEmail());
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailCertificationRequestDto requestDto) {
        boolean isSent = allUserService.sendEmailCertification(requestDto.getId(), requestDto.getEmail());

        if (!isSent) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID가 이미 존재하거나 메일 전송 실패");
        }

        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody CheckCertificationRequestDto requestDto) {
        boolean isVerified = allUserService.verifyCertificationCode(
            requestDto.getId(), requestDto.getEmail(), requestDto.getCertificationNumber()
        );

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 일치하지 않습니다.");
        }

        return ResponseEntity.ok("인증이 완료되었습니다.");
    }


    // 사용자 등록
//    @PostMapping("/register")
//    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
//        logger.info("Registering user: {}", userDto.username()); // 등록 시작 로그
//
//        // 사용자 등록
//        UserDto createdUser = userService.createUser(userDto.username(), userDto.userId(), userDto.password(), userDto.email(), userDto.age());
//
//        logger.info("User registered successfully: {}", createdUser.username()); // 성공적인 등록 로그
//        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
//    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody AllUserDto userDto) {
        if (userDto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid request data"));
        }
        logger.info("Registering user: {}", userDto.getUserId());

        AllUser createdUser = allUserService.registerUser(userDto);
        if (createdUser == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "User registration failed"));
        }

        logger.info("User registered successfully: {}", createdUser.getUserId());

        // 안전하게 token 생성 (nickname 제외, userId만 포함)
        String token = null;
        if (createdUser.getUserId() != null) {
            token = jwtTokenProvider.createToken(
                createdUser.getUserId(),  // userId만 넘기기
                List.of(createdUser.getRole().name())
            );
            logger.info("JWT Token created for user: {}", createdUser.getUserId());
        } else {
            logger.error("User information missing for JWT creation");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "user", Map.of("userId", createdUser.getUserId()),
            "token", token
        ));
    }






//    // 사용자 조회
//    @GetMapping("/show/{userId}")
//    public ResponseEntity<HomeUser> getUser(@PathVariable String userId) {
//        return userService.getUserById(userId)
//                .map(userDto -> {
//                    logger.info("User retrieved: {}", userDto.getUserId()); // 로그 추가
//                    return ResponseEntity.ok(userDto);
//                })
//                .orElseGet(() -> {
//                    logger.warn("User not found: {}", userId); // 로그 추가
//                    return ResponseEntity.notFound().build();
//                });
//    }

    // 사용자 조회
    @GetMapping("/show/{userId}")
    public ResponseEntity<AllUser> getUser(@PathVariable String userId) {
        return allUserService.getUserByUserId(userId)
            .map(userDto -> {
                logger.info("User retrieved: {}", userDto.getUserId()); // 로그 추가
                return ResponseEntity.ok(userDto);
            })
            .orElseGet(() -> {
                logger.warn("User not found: {}", userId); // 로그 추가
                return ResponseEntity.notFound().build();
            });
    }

    // 통합된 allUser 사용자 업데이트
    @PutMapping("/update/{userId}")
    public ResponseEntity<AllUserDto> updateUser(@RequestBody AllUserDto userDto) {
        AllUserDto updatedUser = userService.updateUser(userDto.getUserId(), userDto.getNickname(),userDto.getEmail(),userDto.getAge(),
                                                        userDto.getGender(),userDto.getHeight(),userDto.getWeight());
        return ResponseEntity.ok(updatedUser);
    }

    // 비밀번호 업데이트
    @PutMapping("/password/{userId}")
    public ResponseEntity<Void> updatePassword(@PathVariable String userId, @RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");
        allUserService.updatePassword(userId, password);
        AllUserDto updatedUser = allUserService.updateUserId(userId); // 새로운 userId로 업데이트
        logger.info("Password updated for user: {}", password);
        logger.info("UserId updated for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

    // 일반로그인 탈퇴시 비밀번호 검증
    @PostMapping("/verify-password")
    public ResponseEntity<Void> verifyPassword(@RequestBody Map<String, String> request){
        String userId = request.get("userId");
        String password = request.get("password");


        boolean isValid = allUserService.verifypassword(userId, password);
        if (isValid){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto userDto, HttpServletResponse response) {
//        logger.info("Login attempt for userId: {}", userDto.userId()); // 로그인 시도 로그
//
//        // userId를 통해 사용자 검색
//        HomeUser user = userService.getUserById(userDto.userId())
//            .orElseThrow(() -> {
//                logger.warn("User not found: {}", userDto.userId()); // 사용자 미발견 로그
//                return new UsernameNotFoundException("User not found");
//            });
//
//        // 비밀번호 확인
//        if (!userService.checkPassword(user, userDto.password())) {
//            logger.warn("Invalid password attempt for userId: {}", userDto.userId()); // 잘못된 비밀번호 로그
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
//        }
//
//        // JWT 토큰 생성
//        String token = jwtTokenProvider.createToken(user.getUsername(), user.getUserId(), List.of(user.getRole().getValue()));
//
//        logger.info("User logged in successfully: {}", user.getUsername()); // 성공적인 로그인 로그
//
////        Cookie cookie = new Cookie("AccessToken", token);
////        cookie.setHttpOnly(true); // JavaScript로 접근 불가능하여 XSS 공격 방지
////        cookie.setSecure(false); // HTTPS에서만 전송 (로컬 개발 시 false로 설정 가능)
////        cookie.setPath("/");
////        cookie.setMaxAge(60 * 60); // 1시간 (토큰 만료 시간과 동일하게 설정)
////        response.addCookie(cookie);
//
//        // 사용자 정보와 토큰을 포함한 응답
//        return ResponseEntity.ok(Map.of("user", Map.of("username", user.getUsername()), "token", token));
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AllUserDto userDto) {
        logger.info("Login attempt for userId: {}", userDto.getUserId()); // 수정된 DTO 접근 방식

        // userId를 통해 사용자 검색
        AllUser user = allUserService.getUserByUserId(userDto.getUserId())
            .orElseThrow(() -> {
                logger.warn("User not found: {}", userDto.getUserId()); // 사용자 미발견 로그
                return new UsernameNotFoundException("User not found");
            });

        // 비밀번호 확인 (소셜 로그인인 경우 비밀번호는 없으므로 확인할 필요 없음)
        if (user.getProvider() == LoginType.general && !allUserService.checkPassword(user, userDto.getPassword())) {
            logger.warn("Invalid password attempt for userId: {}", userDto.getUserId()); // 잘못된 비밀번호 로그
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(
            user.getUserId(),
           // user.getNickname(),
            List.of(user.getRole().name())
        );
        logger.info("User logged in successfully: {}", user.getUserId()); // 성공적인 로그인 로그

        // 사용자 정보와 토큰을 포함한 응답
        return ResponseEntity.ok(Map.of(
            "user", Map.of("username", user.getNickname()),
            "token", token
        ));
    }

    @PutMapping("/addUserInfo/{userId}")
    public ResponseEntity<AllUserDto> updateUserInfo(@PathVariable String userId, @RequestBody AllUserDto userDto) {
        try {
            // 서비스 레이어를 호출하여 사용자 정보 업데이트
            AllUser updatedUser = allUserService.updateUserInfo(userId, userDto);

            // 업데이트된 사용자 정보를 DTO로 반환
            AllUserDto responseDto = new AllUserDto(updatedUser);
            return ResponseEntity.ok(responseDto);
        } catch (UserNotFoundException e) {
            logger.info("업데이트 불가능"); // 성공적인 로그인 로그

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/social_user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 사용자 정보를 가져옵니다.
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        // 사용자 정보를 Optional에서 안전하게 추출합니다.
        Optional<AllUser> alluser = allUserRepository.findByUserId(user.getUsername());

        // 만약 사용자 정보가 없다면 404 Not Found 응답
        if (alluser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // 최신 사용자 정보로 갱신
        AllUser updatedUser = alluser.get();
        user.setNickname(updatedUser.getNickname());
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        boolean profileComplete = user.isProfileComplete();
        System.out.println("Profile Complete in UserController Status: " + profileComplete);  // 디버깅용 로그 추가


        // 사용자 정보 맵에 추가
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("social_userId", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("social_nickname", user.getNickname());
        userInfo.put("isSocialUserComplete",profileComplete);  // 추가 정보 여부 추가

        // 사용자 정보 로그 출력
        System.out.println("User Info: " + userInfo);
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/update/social/{userId}")
    public ResponseEntity<?> updateSocialUser(
        @PathVariable String userId,
        @RequestBody SocialUserDTO socialUserDTO) {
        try {
            // 사용자 정보를 수정하는 서비스 호출
            SocialUserDTO updatedUser = userService.updateSocialUser(userId, socialUserDTO);
            return ResponseEntity.ok(updatedUser); // 성공적으로 수정된 사용자 정보 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다."); // 오류 메시지 반환
        }
    }

    // 소셜 사용자 탈퇴
    @DeleteMapping("delete/social_user/{username}")
    public ResponseEntity<Void> deleteSocialUser(@PathVariable String username) {
        try {
            userService.deleteSocialUser(username);
            logger.info("Social user deleted: {}", username); // 로그 추가
            return ResponseEntity.noContent().build(); // 성공시 204 No Content 반환
        } catch (Exception e) {
            logger.error("Error deleting social user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 오류시 500 에러 반환
        }
    }

    // allUser 사용자 삭제
    @DeleteMapping("delete/{userId}")
    public ResponseEntity<Void> deleteAllUser(@PathVariable String userId) {
        try {
            allUserService.deleteUser(userId);
            logger.info("alluser deleted: {}", userId); // 로그 추가
            return ResponseEntity.noContent().build(); // 성공시 204 No Content 반환
        } catch (Exception e) {
            logger.error("Error deleting social user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 오류시 500 에러 반환
        }
    }

    @PostMapping("/style")
    public ResponseEntity<String> saveUserStyles(@RequestBody Map<String, Object> requestData) {
        try {
            String userId = (String) requestData.get("userId");
            List<?> rawStyles = (List<?>) requestData.get("preferences");


            // 스트림을 사용하여 String 타입 요소만 필터링
            List<String> styles = rawStyles.stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .toList();


            allUserService.saveUserStyles(userId, styles);
            return ResponseEntity.ok("User styles saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data format");
        }
    }

    @GetMapping("/style/{userId}")
    public ResponseEntity<List<UserStyle>> getUserStyle(@PathVariable("userId") String userId) {
        List<UserStyle> userStyles = allUserService.getUserStyle(userId);
        if (userStyles != null && !userStyles.isEmpty()) {
            return ResponseEntity.ok(userStyles); // 스타일 리스트 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 스타일이 없으면 404 반환
        }

    }
}