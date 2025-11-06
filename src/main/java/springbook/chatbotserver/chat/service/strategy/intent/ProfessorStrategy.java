package springbook.chatbotserver.chat.service.strategy.intent;

import org.springframework.stereotype.Component;

import springbook.chatbotserver.chat.model.domain.Professor;
import springbook.chatbotserver.chat.model.mapper.ProfessorMapper;
import springbook.chatbotserver.chat.service.strategy.AbstractIntentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

/**
 * Rasa 챗봇의 'ask_office_of_professor' 인텐트를 처리하는 전략 클래스입니다.
 * 사용자가 특정 교수님의 교수실 위치를 요청할 때 해당 교수의 정보를 조회하여 응답을 생성합니다.
 */
@Component
public class ProfessorStrategy extends AbstractIntentStrategy {

  private final ProfessorMapper professorMapper;

  public ProfessorStrategy(ProfessorMapper professorMapper) {
    super("ask_office_of_professor", "professor");
    this.professorMapper = professorMapper;
  }

  @Override
  public String handleEntityValue(String entityValue) {

    Professor professor = professorMapper.findOfficeByProfessorName(entityValue);

    if (professor == null) {
      throw new CustomException(ErrorCode.PROFESSOR_NOT_FOUND);
    }

    return professorLocationMessage(professor, entityValue);
  }

  private String professorLocationMessage(Professor professor, String professorName) {
    return String.format("%s 교수님의 교수실은 %s %s에 있습니다.\n%s",
        professorName, professor.getBuildingName(), professor.getOffice(), professor.getMapUrl());
  }
}