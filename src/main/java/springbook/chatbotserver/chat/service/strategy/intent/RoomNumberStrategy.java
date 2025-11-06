package springbook.chatbotserver.chat.service.strategy.intent;

import org.springframework.stereotype.Component;

import springbook.chatbotserver.chat.model.domain.Building;
import springbook.chatbotserver.chat.model.dto.RoomInfo;
import springbook.chatbotserver.chat.model.mapper.BuildingMapper;
import springbook.chatbotserver.chat.service.strategy.AbstractIntentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇의 'ask_room_location' 인텐트를 처리하는 전략 클래스입니다.
 * 사용자가 강의실 위치를 요청할 때, 해당 강의실의 건물과 위치 정보를 조회하여 응답을 생성합니다.
 */
@Component
public class RoomNumberStrategy extends AbstractIntentStrategy {

  private final BuildingMapper buildingMapper;

  public RoomNumberStrategy(BuildingMapper buildingMapper) {
    super("ask_room_location", "room_number");
    this.buildingMapper = buildingMapper;
  }

  @Override
  public String handleEntityValue(String entityValue) {

    RoomInfo roomInfo = parseRoomInfo(entityValue);
    if (!roomInfo.isValid()) {
      throw new CustomException(ErrorCode.INVALID_ROOM_NUMBER);
    }
    Building building = buildingMapper.findByBuildingNumber(roomInfo.getBuildingNumber());
    if (building == null) {
      throw new CustomException(ErrorCode.BUILDING_NOT_FOUND);
    }

    return buildLocationMessage(building, roomInfo.getRoomNumber());
  }

  private RoomInfo parseRoomInfo(String rawCode) {
    String code = rawCode.replaceAll("[^0-9]", "");

    if (code.length() < 4) {
      return RoomInfo.invalid();
    }
    int lastIndex = code.length();
    int buildingNumber = Integer.parseInt(code.substring(0, lastIndex - 3));
    int roomNumber = Integer.parseInt(code.substring(lastIndex - 3, lastIndex));

    return new RoomInfo(buildingNumber, roomNumber);
  }

  private String buildLocationMessage(Building building, int roomNumber) {
    return String.format("해당 강의실은 %s %d호에 있습니다.\n건물 위치는 다음과 같습니다.\n%s",
        building.getName(), roomNumber, building.getMapUrl());
  }
}


