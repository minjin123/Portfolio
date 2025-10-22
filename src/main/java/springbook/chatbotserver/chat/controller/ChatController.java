package springbook.chatbotserver.chat.controller;

import static springbook.chatbotserver.http.ResponseCode.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import springbook.chatbotserver.chat.model.dto.RasaRequest;
import springbook.chatbotserver.chat.service.RasaService;
import springbook.chatbotserver.http.HttpResponseBody;

/**
 * Rasa 챗봇과 통신하는 컨트롤러입니다.
 * 사용자의 요청을 받아 Rasa 서버로 전송하고, 응답을 반환합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "rasa", description = "rasa 챗봇과 얘기 나누는 api 입니다...")
public class ChatController {

  private final RasaService rasaService;

  /**
   * Rasa 챗봇에 메시지를 전송하고, 응답을 받아 반환합니다.
   *
   * @param request RasaRequest 객체로, 사용자를 식별하기 위한 deviceId와
   *                요청 메시지인 text가 포함되어 있습니다.
   * @return ResponseEntity<Object> 객체로, Rasa 챗봇의 응답 메시지를 포함합니다.
   */
  @PostMapping
  public ResponseEntity<Object> chat(@RequestBody RasaRequest request) {
    String message = rasaService.sendMessageToRasa(request);

    return HttpResponseBody.builder()
        .code(HttpStatus.OK.value())
        .subCode(NOT_ISSUE.getSubCode())
        .message(NOT_ISSUE.getMessage())
        .response(message)
        .build();
  }
}
