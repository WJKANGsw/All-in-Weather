package com.spring.model.dto.response.auth;

import com.spring.common.ResponseCode;
import com.spring.common.ResponseMessage;
import com.spring.model.dto.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



import lombok.Getter;

@Getter
public class EmailCertificationResponseDto extends ResponseDto {
  private EmailCertificationResponseDto() {
    super();
  }

  public static ResponseEntity<EmailCertificationResponseDto> success() {
    EmailCertificationResponseDto responseBody = new EmailCertificationResponseDto();
    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
  }

  public static ResponseEntity<ResponseDto> duplicateId() {
    ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
  }

  public static ResponseEntity<ResponseDto> mailSendFail() {
    ResponseDto responseBody = new ResponseDto(ResponseCode.MAIL_FAIL, ResponseMessage.DUPLICATE_ID);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
  }

  
}
