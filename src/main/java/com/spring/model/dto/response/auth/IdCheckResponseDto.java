package com.spring.model.dto.response.auth;

import com.spring.common.ResponseCode;
import com.spring.common.ResponseMessage;
import com.spring.model.dto.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import lombok.Getter;

@Getter
public class IdCheckResponseDto extends ResponseDto {
  private IdCheckResponseDto() {
    super();
  }

  public static ResponseEntity<IdCheckResponseDto> success() {
    IdCheckResponseDto responseBody = new IdCheckResponseDto();
    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
  }

  public static ResponseEntity<ResponseDto> duplicateId() {
    ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
  }
  
}
