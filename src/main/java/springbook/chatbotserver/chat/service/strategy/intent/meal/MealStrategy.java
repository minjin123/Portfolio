package springbook.chatbotserver.chat.service.strategy.intent.meal;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import springbook.chatbotserver.chat.model.dto.MealResponse;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.model.mapper.BuildingMapper;
import springbook.chatbotserver.chat.model.mapper.MealMapper;
import springbook.chatbotserver.chat.service.strategy.IntentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇의 'ask_meal_of_dormitory' 인텐트를 처리하는 전략 클래스입니다.
 * 사용자로부터 받은 기숙사명, 날짜, 식사 유형 등을 기반으로 식단 정보를 조회하여 응답을 구성합니다.
 */
@Component
@RequiredArgsConstructor
public class MealStrategy implements IntentStrategy {
  private final BuildingMapper buildingMapper;
  private final MealMapper mealMapper;
  private final MealMessageBuilder mealMessageBuilder;

  /**
   * 이 전략이 처리하는 인텐트(intent) 이름을 반환합니다.
   * @return "ask_meal_of_dormitory"
   */
  @Override
  public String getIntent() {
    return "ask_meal_of_dormitory";
  }

  /**
   * 사용자의 Rasa 응답으로부터 도출한 엔티티 정보를 기반으로 기숙사 식단 정보를 조회하고,
   * 사용자에게 적절한 문자열 형태로 응답을 구성합니다.
   *
   * @param response Rasa에서 추출된 인텐트 및 엔티티 정보
   * @return 식단 정보 또는 안내 메시지 문자열
   */
  @Override
  @Transactional(readOnly = true)
  public String handle(RasaResponse response) {
    String dorm = extract(response, "dorm", "");
    String time = extract(response, "time", "오늘");
    String mealType = extract(response, "meal_type", "ALL");
    Integer buildingNumber = buildingMapper.findBuildingNumberOfBuildingName(dorm);
    if (buildingNumber == null) {
      throw new CustomException(ErrorCode.RESTAURANT_NOT_FOUND);
    }
    if ("이번주".equals(time)) {
      return mealMessageBuilder.buildWeekMenuMessage(dorm);
    }
    String mealDate = DateResolver.resolve(time);
    List<MealResponse> meals = mealMapper.findMealsByDates(buildingNumber, mealDate, mealType);
    if (meals.isEmpty()) {
      throw new CustomException(ErrorCode.MEAL_NOT_FOUND);
    }

    return mealMessageBuilder.buildMealMessage(dorm, mealDate, meals);
  }

  private String extract(RasaResponse response, String key, String defaultValue) {
    return response.getEntities().stream()
        .filter(e -> key.equals(e.getEntity()))
        .map(RasaResponse.Entity::getValue)
        .findFirst()
        .orElse(defaultValue);
  }

}
