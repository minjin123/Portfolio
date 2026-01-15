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
import springbook.chatbotserver.chat.service.strategy.intent.RoomNumberStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class RoomNumberStrategyTest {

    @Mock
    private BuildingMapper buildingMapper;

    @InjectMocks
    private RoomNumberStrategy roomNumberStrategy;
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
    @DisplayName("강의실 넘버로 강의실 위치를 반환한다")
    void execute_ReturnsRoomLocation() {
        //given
        aopContextMock.when(AopContext::currentProxy).thenReturn(roomNumberStrategy);

        String buildingNumber = "24";
        String roomNumber ="24208";
        Building mockBuilding = new Building();
        ReflectionTestUtils.setField(mockBuilding,"buildingNumber",24);
        ReflectionTestUtils.setField(mockBuilding,"name","융합과학관");
        ReflectionTestUtils.setField(mockBuilding,"mapUrl","http://map.url");

        given(buildingMapper.findByBuildingNumber(Integer.valueOf(buildingNumber))).willReturn(mockBuilding);

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "room_number");
        ReflectionTestUtils.setField(entity, "value",roomNumber);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        //when
        String result = roomNumberStrategy.handle(rasaResponse);

        //then
        assertAll(
                () -> assertThat(result).contains("융합과학관"),
                () -> assertThat(result).contains("208호"),
                () -> assertThat(result).contains("http://map.url")
        );
    }

    @Test
    @DisplayName("존재하지 않는 건물 번호일 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenBuildingNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(roomNumberStrategy);

        // 99208 -> 건물번호 99 가정
        String buildingNumber = "99";
        String roomNumber = "99208";

        // DB 조회 시 null 반환 (건물 없음)
        given(buildingMapper.findByBuildingNumber(Integer.valueOf(buildingNumber))).willReturn(null);

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "room_number");
        ReflectionTestUtils.setField(entity, "value", roomNumber);

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> roomNumberStrategy.handle(rasaResponse));


        // ErrorCode.BUILDING_NOT_FOUND (또는 FACILITY_NOT_FOUND) 확인 필요
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BUILDING_NOT_FOUND);
    }
}