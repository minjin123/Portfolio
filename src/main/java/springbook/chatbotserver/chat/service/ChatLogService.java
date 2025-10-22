package springbook.chatbotserver.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import springbook.chatbotserver.chat.model.domain.ChatLog;
import springbook.chatbotserver.chat.model.dto.ChatMessageDto;
import springbook.chatbotserver.chat.model.dto.RasaRequest;
import springbook.chatbotserver.chat.model.repository.ChatLogRepository;

/**
 * 채팅 기록을 조회하는 서비스입니다.
 * 이 서비스는 특정 디바이스 ID에 대한 채팅 로그를 가져오는 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ChatLogService {
  private final ChatLogRepository chatLogRepository;

  /**
   * 주어진 디바이스 ID에 대한 채팅 로그를 조회합니다.
   *
   * @param deviceId 조회할 디바이스의 ID
   * @return 해당 디바이스의 채팅 로그 목록
   */
  @Transactional(readOnly = true)
  public List<ChatMessageDto> getChatLogs(String deviceId) {
    return chatLogRepository.findByDeviceIdOrderByTimestampAsc(deviceId)
        .stream()
        .map(ChatMessageDto::from)
        .toList();
  }

  /**
   * 사용자의 메시지를 채팅 로그에 저장합니다.
   *
   * @param req RasaRequest 객체로, 사용자의 디바이스 ID와 메시지 텍스트를 포함합니다.
   */
  @Transactional
  public void saveUserMessage(RasaRequest req) {
    chatLogRepository.save(ChatLog.builder()
        .deviceId(req.getDeviceId())
        .timestamp(LocalDateTime.now())
        .messageType("user")
        .text(req.getText())
        .build());
  }
  /**
   * 챗봇의 메시지를 채팅 로그에 저장합니다.
   *
   * @param req RasaRequest 객체로, 사용자의 디바이스 ID를 포함합니다.
   * @param botText 챗봇의 응답 메시지 텍스트
   */
  @Transactional
  public void saveBotMessage(RasaRequest req, String botText) {
    chatLogRepository.save(ChatLog.builder()
        .deviceId(req.getDeviceId())
        .timestamp(LocalDateTime.now())
        .messageType("bot")
        .text(botText)
        .build());
  }
}
