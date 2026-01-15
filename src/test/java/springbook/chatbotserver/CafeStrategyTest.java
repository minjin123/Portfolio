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
import springbook.chatbotserver.chat.model.domain.Facility;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.model.mapper.FacilityMapper;
import springbook.chatbotserver.chat.service.strategy.intent.facility.CafeStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class CafeStrategyTest {

    @Mock
    private FacilityMapper facilityMapper;

    @InjectMocks
    private CafeStrategy cafeStrategy;

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
    @DisplayName("카페 위치 정보를 조회하여 반환한다")
    void execute_ReturnsCafeLocations() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(cafeStrategy);

        Facility facility = new Facility();
        ReflectionTestUtils.setField(facility, "name", "도서관 카페");
        ReflectionTestUtils.setField(facility, "locationDetail", "2층");
        ReflectionTestUtils.setField(facility, "mapUrl", "http://map.url/cafe");

        // "카페" 입력 시 "cafe" 타입으로 조회
        given(facilityMapper.findByFacilityType("cafe")).willReturn(List.of(facility));

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "카페");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when
        String result = cafeStrategy.handle(rasaResponse);

        // then
        assertAll(
                () -> assertThat(result).contains("도서관 카페"),
                () -> assertThat(result).contains("2층"),
                () -> assertThat(result).contains("http://map.url/cafe")
        );
    }

    @Test
    @DisplayName("카페 정보가 없을 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenCafeNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(cafeStrategy);

        given(facilityMapper.findByFacilityType("cafe")).willReturn(Collections.emptyList());

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "카페");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> cafeStrategy.handle(rasaResponse));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FACILITY_NOT_FOUND);
    }
}