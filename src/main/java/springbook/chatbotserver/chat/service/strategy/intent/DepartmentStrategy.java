package springbook.chatbotserver.chat.service.strategy.intent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import springbook.chatbotserver.chat.model.domain.Building;
import springbook.chatbotserver.chat.model.mapper.BuildingMapper;
import springbook.chatbotserver.chat.service.strategy.AbstractIntentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇의 'ask_building_of_department' 인텐트를 처리하는 전략 클래스입니다.
 * 사용자가 특정 학과의 위치를 질문하면, 해당 학과가 소속된 건물 정보를 조회하여 응답을 생성합니다.
 */
@Component
public class DepartmentStrategy extends AbstractIntentStrategy {

  private final BuildingMapper buildingMapper;

  public DepartmentStrategy(BuildingMapper buildingMapper) {
    super("ask_building_of_department", "department");
    this.buildingMapper = buildingMapper;
  }

  @Override
  @Cacheable(cacheNames = "department")
  public String handleEntityValue(String entityValue) {

    String departmentName = extractDepartmentName(entityValue);

    Building building = buildingMapper.findBuildingNameOfDepartment(departmentName);

    if (building == null) {
      throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    return departmentLocationMessage(building, departmentName);
  }

  private String extractDepartmentName(String rawCode) {
    Pattern pattern = Pattern.compile("^(.*?과)(은|는|이|가|를|의|에|으로)?$");
    Matcher matcher = pattern.matcher(rawCode);
    return matcher.find() ? matcher.group(1) : rawCode;
  }

  private String departmentLocationMessage(Building building, String departmentName) {
    return String.format("%s는 %s에 있습니다.\n%s",
        departmentName, building.getName(), building.getMapUrl());
  }
}