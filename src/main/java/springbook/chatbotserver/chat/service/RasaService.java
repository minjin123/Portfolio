package springbook.chatbotserver.chat.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import springbook.chatbotserver.chat.model.dto.RasaRequest;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.service.strategy.IntentStrategy;
import springbook.chatbotserver.chat.service.strategy.StrategyFactory;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇과의 통신 및 인텐트 처리 전략 실행을 담당하는 서비스 클래스입니다.
 * 사용자 메시지를 Rasa 서버에 전송하고, 응답 결과를 기반으로 적절한 전략을 적용하여 응답을 생성합니다.
 */
@Service
@RequiredArgsConstructor
public class RasaService {

  private final RestTemplate restTemplate;
  private final StrategyFactory strategyFactory;
  private final ChatLogService chatLogService;

  @Value("${IP}")
  private String ip;

  /**
   * 사용자 요청 메시지를 Rasa 서버로 전송하고,
   * 응답에 포함된 인텐트를 기반으로 적절한 전략을 실행하여 결과를 반환합니다.
   * 또한 사용자/챗봇의 메시지를 로그로 저장합니다.
   *
   * @param req 사용자 요청 DTO (deviceId, message 포함)
   * @return 챗봇 응답 메시지
   */
  public String sendMessageToRasa(RasaRequest req) {
    // 사용자 메시지 로그 저장
    chatLogService.saveUserMessage(req);
    // Rasa 서버에 POST 요청
    RasaResponse rasa = getRasaResponse(req);

    // 전략 실행
    String botMessage;
    try {
     botMessage = handleIntent(rasa);
    } catch (CustomException e) {
      botMessage = e.getMessage();
    }
    // 챗봇 응답 메시지 로그 저장
    chatLogService.saveBotMessage(req, botMessage);

    return botMessage;
  }


  private RasaResponse getRasaResponse(RasaRequest req) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<RasaRequest> entity = new HttpEntity<>(req, headers);
    String rasaUrl = "http://" + ip + ":5005/model/parse";
    ResponseEntity<RasaResponse> response = restTemplate.postForEntity(
        rasaUrl,
        entity,
        RasaResponse.class
    );

      return response.getBody();
  }

  private String handleIntent(RasaResponse rasa) {
    String intent = rasa.getIntent().getName();
    IntentStrategy strategy = strategyFactory.getStrategy(intent);
    return strategy.handle(rasa);
  }

}