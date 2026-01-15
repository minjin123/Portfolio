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
import springbook.chatbotserver.chat.service.strategy.intent.facility.StoreStrategy;
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
public class StoreStrategyTest {

    @Mock
    private FacilityMapper facilityMapper;

    @InjectMocks
    private StoreStrategy storeStrategy;

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
    @DisplayName("매점 위치 정보를 조회하여 반환한다")
    void execute_ReturnsStoreLocations() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(storeStrategy);

        // Mock Data: Facility (매점)
        Facility facility1 = new Facility();
        ReflectionTestUtils.setField(facility1, "name", "공학관 매점");
        ReflectionTestUtils.setField(facility1, "locationDetail", "1층 로비");
        ReflectionTestUtils.setField(facility1, "mapUrl", "http://map.url/1");

        Facility facility2 = new Facility();
        ReflectionTestUtils.setField(facility2, "name", "도서관 매점");
        ReflectionTestUtils.setField(facility2, "locationDetail", "지하 1층");
        ReflectionTestUtils.setField(facility2, "mapUrl", "http://map.url/2");

        // "매점" 입력 시 "store" 타입으로 조회됨 (StoreStrategy 내부 Map 확인)
        given(facilityMapper.findByFacilityType("store")).willReturn(List.of(facility1, facility2));

        // Mock Data: RasaResponse (Input)
        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "매점");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when
        String result = storeStrategy.handle(rasaResponse);

        // then
        assertAll(
                () -> assertThat(result).contains("공학관 매점"),
                () -> assertThat(result).contains("1층 로비"),
                () -> assertThat(result).contains("http://map.url/1"),
                () -> assertThat(result).contains("도서관 매점"),
                () -> assertThat(result).contains("지하 1층"),
                () -> assertThat(result).contains("http://map.url/2")
        );
    }

    @Test
    @DisplayName("매점 정보가 없을 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenStoreNotFound() {
        // given
        aopContextMock.when(AopContext::currentProxy).thenReturn(storeStrategy);

        // DB 조회 결과가 없을 때
        given(facilityMapper.findByFacilityType("store")).willReturn(Collections.emptyList());

        RasaResponse.Entity entity = new RasaResponse.Entity();
        ReflectionTestUtils.setField(entity, "entity", "facility");
        ReflectionTestUtils.setField(entity, "value", "매점");

        RasaResponse rasaResponse = new RasaResponse();
        ReflectionTestUtils.setField(rasaResponse, "entities", List.of(entity));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> storeStrategy.handle(rasaResponse));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FACILITY_NOT_FOUND);
    }
}