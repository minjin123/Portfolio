package springbook.chatbotserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.AopContext;
import org.springframework.test.util.ReflectionTestUtils;
import springbook.chatbotserver.chat.model.domain.Professor;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.model.mapper.ProfessorMapper;
import springbook.chatbotserver.chat.service.strategy.intent.ProfessorStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ProfessorStrategyTest {
    @Mock
    private ProfessorMapper professorMapper;

    @InjectMocks
    private ProfessorStrategy professorStrategy;

    private MockedStatic<AopContext> aopContextMock;

    @BeforeEach
    void setUp() {
        aopContextMock = mockStatic(AopContext.class);
    }
    @AfterEach
    void tearDown() {
        aopContextMock.close();
    }

    @Test
    @DisplayName("교수님 이름으로 교수실 위치를 반환한다")
    void execute_ReturnsProfessorOfficeLocation() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(professorStrategy);

        String professorName = "홍길동";
        Professor mockProfessor = new Professor();
        ReflectionTestUtils.setField(mockProfessor, "buildingName", "융합과학관");
        ReflectionTestUtils.setField(mockProfessor, "office", "208호");
        ReflectionTestUtils.setField(mockProfessor, "mapUrl", "http://map.url");

        given(professorMapper.findOfficeByProfessorName(professorName)).willReturn(mockProfessor);


        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "professor");
        ReflectionTestUtils.setField(entity, "value", professorName);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when
        String result = professorStrategy.handle(rasaResponse);

        // then
        assertAll(
                () -> assertThat(result).contains("홍길동"),
                () -> assertThat(result).contains("융합과학관"),
                () -> assertThat(result).contains("208호"),
                () -> assertThat(result).contains("http://map.url")
        );
    }

    @Test
    @DisplayName("존재하지 않는 교수님일 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenProfessorNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(professorStrategy);

        String professorName = "없는교수";
        // DB 조회 시 null 반환
        given(professorMapper.findOfficeByProfessorName(professorName)).willReturn(null);

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "professor");
        ReflectionTestUtils.setField(entity, "value", professorName);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> professorStrategy.handle(rasaResponse));

        // ErrorCode.PROFESSOR_NOT_FOUND 확인 필요
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROFESSOR_NOT_FOUND);
    }
}