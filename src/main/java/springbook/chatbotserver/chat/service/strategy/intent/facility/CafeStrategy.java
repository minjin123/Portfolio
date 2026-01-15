package springbook.chatbotserver.chat.service.strategy.intent.facility;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import springbook.chatbotserver.chat.model.domain.Facility;
import springbook.chatbotserver.chat.model.mapper.FacilityMapper;
import springbook.chatbotserver.chat.service.strategy.AbstractIntentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇의 'ask_location_of_cafe' 인텐트를 처리하는 전략 클래스입니다.
 * 사용자가 카페의 위치를 요청할 때, 해당 카페 정보를 조회하여 응답을 생성합니다.
 */
@Component
public class CafeStrategy extends AbstractIntentStrategy {

  private static final Map<String, String> facilityMap = Map.of(
      "카페", "cafe",
      "교내카페", "cafe"
  );
  private final FacilityMapper facilityMapper;

  public CafeStrategy(FacilityMapper facilityMapper) {
    super("ask_location_of_cafe", "facility");
    this.facilityMapper = facilityMapper;
  }

  @Override
  public String handleEntityValue(String entityValue) {

    String facilityType = facilityMap.getOrDefault(entityValue, "");
    List<Facility> facilities = facilityMapper.findByFacilityType(facilityType);
    if (facilities.isEmpty()) {
      throw new CustomException(ErrorCode.FACILITY_NOT_FOUND);
    }

    return cafeLocationMessage(facilities);
  }

  private String cafeLocationMessage(List<Facility> facilities) {
    StringBuilder sb = new StringBuilder();
    sb.append("다음은 카페가 위치한 장소입니다:\n\n");
    for (Facility facility : facilities) {
      sb.append("- ")
          .append(facility.getName())
          .append(" (")
          .append(facility.getLocationDetail())
          .append(")\n")
          .append(facility.getMapUrl())
          .append("\n\n");
    }
    return sb.toString().trim();
  }
}

