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
import springbook.chatbotserver.chat.service.strategy.intent.facility.AtmStrategy;
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
public class AtmStrategyTest {

    @Mock
    private FacilityMapper facilityMapper;

    @InjectMocks
    private AtmStrategy atmStrategy;

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
    @DisplayName("ATM 위치 정보를 조회하여 반환한다")
    void execute_ReturnsAtmLocations() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(atmStrategy);

        Facility facility = new Facility();
        ReflectionTestUtils.setField(facility, "name", "학생회관 ATM");
        ReflectionTestUtils.setField(facility, "locationDetail", "1층 입구");
        ReflectionTestUtils.setField(facility, "mapUrl", "http://map.url/atm");

        // "ATM" 입력 시 "atm" 타입으로 조회
        given(facilityMapper.findByFacilityType("atm")).willReturn(List.of(facility));

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "ATM");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when
        String result = atmStrategy.handle(rasaResponse);

        // then
        assertAll(
                () -> assertThat(result).contains("학생회관 ATM"),
                () -> assertThat(result).contains("1층 입구"),
                () -> assertThat(result).contains("http://map.url/atm")
        );
    }

    @Test
    @DisplayName("ATM 정보가 없을 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenAtmNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(atmStrategy);

        given(facilityMapper.findByFacilityType("atm")).willReturn(Collections.emptyList());

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "ATM");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> atmStrategy.handle(rasaResponse));
        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FACILITY_NOT_FOUND);
    }
}