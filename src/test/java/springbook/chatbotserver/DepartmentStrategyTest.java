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
import springbook.chatbotserver.chat.model.domain.Building;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.model.mapper.BuildingMapper;
import springbook.chatbotserver.chat.service.strategy.intent.DepartmentStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class DepartmentStrategyTest {

    @Mock
    private BuildingMapper buildingMapper;

    @InjectMocks
    private DepartmentStrategy departmentStrategy;

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
    @DisplayName("학과명으로 학과가 위치한 건물을 반환한다.")
    void execute_ReturnsDepartmentLocation() {
        //given
        aopContextMock.when(AopContext::currentProxy).thenReturn(departmentStrategy);

        String department = "정보통신공학과";
        Building mockBuilding = new Building();
        ReflectionTestUtils.setField(mockBuilding,"name","융합과학관");
        ReflectionTestUtils.setField(mockBuilding, "mapUrl", "http://map.url");

        given(buildingMapper.findBuildingNameOfDepartment(department)).willReturn(mockBuilding);

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "department");
        ReflectionTestUtils.setField(entity, "value",department);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));
        //when
        String result = departmentStrategy.handle(rasaResponse);
        //then
        assertAll(
                () -> assertThat(result).contains("융합과학관"),
                () -> assertThat(result).contains("정보통신공학과"),
                () -> assertThat(result).contains("http://map.url")
        );
    }

    @Test
    @DisplayName("존재하지 않는 학과일 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenDepartmentNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(departmentStrategy);

        String department = "존재하지않는학과";
        // DB 조회 시 null 반환 가정
        given(buildingMapper.findBuildingNameOfDepartment(department)).willReturn(null);

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "department");
        ReflectionTestUtils.setField(entity, "value", department);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> departmentStrategy.handle(rasaResponse));

        // ErrorCode.DEPARTMENT_NOT_FOUND가 맞는지 확인 필요
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
    }
}