package springbook.chatbotserver.chat.service.strategy;

import org.springframework.transaction.annotation.Transactional;

import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * 개별 전략 클래스들이 상속하여 사용할 수 있는 템플릿 클래스입니다.
 * 특정 인텐트와 관련된 엔티티 값을 추출하고, 이를 처리하는 공통 로직을 제공합니다.
 */
public abstract class AbstractIntentStrategy implements IntentStrategy {

  private final String intentName;
  private final String entityName;

  public AbstractIntentStrategy(String intentName, String entityName) {
    this.intentName = intentName;
    this.entityName = entityName;
  }

  @Override
  public String getIntent() {
    return intentName;
  }

  @Override
  @Transactional(readOnly = true)
  public String handle(RasaResponse response) {
    String entityValue = getEntityValue(response, entityName);
    if (entityValue.isBlank()) {
      throw new CustomException(ErrorCode.INTENT_NOT_FOUND);
    }
    return handleEntityValue(entityValue);
  }

  private String getEntityValue(RasaResponse response, String entityName) {
    return response.getEntities().stream()
        .filter(e -> entityName.equals(e.getEntity()))
        .map(RasaResponse.Entity::getValue)
        .findFirst()
        .orElse("");
  }

  /**
   * 추출된 엔티티 값을 처리하는 메서드입니다.
   * 각 전략 클래스에서 이 메서드를 구현하여 엔티티 값을 기반으로 응답 메시지를 생성합니다.
   *
   * @param entityValue 추출된 엔티티 값
   * @return 처리된 응답 메시지
   */
  protected abstract String handleEntityValue(String entityValue);
}
