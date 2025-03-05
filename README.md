All-in-weather 생성형ai(ChatGPT API & DALL E)를 활용하여 사용자에게 날씨 정보와 옷차림을 추천해주는 웹서비스 입니다.(모바일 최적화 & pwa 방식으로 빌드)

BE
1. sns 간편회원가입(구글,카카오,네이버) - 스프링 OAuth2 클라이언트 JWT (https://www.devyummi.com/page?id=669296ed4b5dc5675c8737be)
2. 일반회원가입(이메일 인증) - https://github.com/WJKANGsw/spring-oauth-mail
3. refresh token

4. FE 에서 ChatGPT API로 얻은 추천옷차림 텍스트값을 dall e 프롬프트에 대입 후 추출된 이미지를 s3 url 변환 후 백엔드로 전송
  - aws s3에 이미지 저장

6. 일정알림
  - 온라인 상태 : 앱을 사용중 일때는 in-app-notification 형태로 알림을 전송
  - 오프라인 상태 : FCM(Firebase Cloud Messaging)를 이용해 푸시 알림을 전송
