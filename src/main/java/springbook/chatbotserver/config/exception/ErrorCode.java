package springbook.chatbotserver.config.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러코드 및 메시지를 정의하는 열거형 클래스입니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 400 Bad Request
  INVALID_ROOM_NUMBER(HttpStatus.BAD_REQUEST, 1000, "강의실 번호가 올바르지 않습니다. 다시 입력해주세요."),
  INVALID_date(HttpStatus.BAD_REQUEST, 1001, "날짜 형식이 올바르지 않습니다. ex) 월요일, 오늘, 6월 13일 등"),
  // 404 Not Found
  FACILITY_NOT_FOUND(HttpStatus.NOT_FOUND, 1002, "해당 시설은 존재하지 않습니다. 다시 입력해주세요."),
  DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "해당 학과는 존재하지 않습니다. 다시 입력해주세요."),
  PROFESSOR_NOT_FOUND(HttpStatus.NOT_FOUND, 1004, "해당 교수님은 존재하지 않습니다. 다시 입력해주세요."),
  BUILDING_NOT_FOUND(HttpStatus.NOT_FOUND, 1005, "해당 강의실이 있는 건물은 존재하지 않습니다. 다시 입력해주세요."),
  RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, 1006, "해당 식당은 존재하지 않습니다. 다시 입력해주세요."),
  MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, 1007, "해당 날짜의 식단은 존재하지 않습니다."),
  INTENT_NOT_FOUND(HttpStatus.NOT_FOUND, 1008, "죄송합니다. 해당 요청을 처리할 수 없습니다. 다른 질문 부탁드립니다."),
  TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, 1009, "해당 식단 테이블이 존재하지 않습니다."),
  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
  RASA_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Rasa 서버와의 통신 중 오류가 발생했습니다."),
  FAILED_CRAWLING(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "크롤링에 실패했습니다."),
  // 504 Gateway Timeout
  CONNECTION_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, 5003, "사용자가 너무 많아서 요청을 처리하지 못하고 있습니다. 잠시 후 다시 시도해주세요.");

  private final HttpStatus status;
  private final int subCode;
  private final String message;
}
