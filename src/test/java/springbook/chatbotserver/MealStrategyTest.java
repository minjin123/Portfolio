package springbook.chatbotserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy; // Spy 사용
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import springbook.chatbotserver.chat.model.dto.MealResponse;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.model.mapper.BuildingMapper;
import springbook.chatbotserver.chat.model.mapper.MealMapper;
import springbook.chatbotserver.chat.service.strategy.intent.meal.DateResolver;
import springbook.chatbotserver.chat.service.strategy.intent.meal.MealMessageBuilder;
import springbook.chatbotserver.chat.service.strategy.intent.meal.MealStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class MealStrategyTest {

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private MealMapper mealMapper;

    @Spy
    private MealMessageBuilder mealMessageBuilder;

    @InjectMocks
    private MealStrategy mealStrategy;

    private MockedStatic<DateResolver> dateResolverMock;

    @BeforeEach
    void setUp() {
        dateResolverMock = mockStatic(DateResolver.class);
    }

    @AfterEach
    void tearDown() {
        dateResolverMock.close();
    }

    @Test
    @DisplayName("특정 날짜의 식단을 요청할 경우 메뉴 상세 내용이 포함된 메시지를 반환한다")
    void execute_ResolvesDateAndReturnsDetailedMealMessage() {
        // given
        String dormName = "학생회관";
        String time = "오늘";
        String fixedDate = "2025-05-15";


        given(buildingMapper.findBuildingNumberOfBuildingName(dormName)).willReturn(202);


        dateResolverMock.when(() -> DateResolver.resolve(time)).thenReturn(fixedDate);


        MealResponse meal1 = MealResponse.builder()
                .mealDate(fixedDate)
                .mealType("LUNCH")
                .menuItem("돈가스")
                .build();
        List<MealResponse> meals = List.of(meal1);


        given(mealMapper.findMealsByDates(eq(202), eq(fixedDate), anyString()))
                .willReturn(meals);


        RasaResponse rasaRequest = createRasaResponse(dormName, time, "lunch");

        // when
        String result = mealStrategy.handle(rasaRequest);

        // then
        assertThat(result).contains("돈가스");
        assertThat(result).contains(fixedDate);
        assertThat(result).contains(dormName.toUpperCase());
    }

    @Test
    @DisplayName("이번주 식단을 요청할 경우 날짜 선택 안내 메시지를 반환한다")
    void execute_ReturnsDateSelectionGuide_WhenWeekRequested() {
        // given
        String dormName = "기숙사식당";
        String time = "이번주";

        given(buildingMapper.findBuildingNumberOfBuildingName(dormName)).willReturn(101);

        dateResolverMock.when(DateResolver::getWeekDates).thenReturn(List.of("2025-05-15"));

        RasaResponse rasaRequest = createRasaResponse(dormName, time, "ALL");

        // when
        String result = mealStrategy.handle(rasaRequest);

        // then
        assertThat(result).contains("식단 정보는 다음 날짜에 확인하실 수 있습니다");
    }


    @Test
    @DisplayName("식당(기숙사) 정보가 없을 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenRestaurantNotFound() {
        // given
        String dormName = "없는식당";
        given(buildingMapper.findBuildingNumberOfBuildingName(dormName)).willReturn(null);
        RasaResponse rasaRequest = createRasaResponse(dormName, "오늘", "ALL");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> mealStrategy.handle(rasaRequest));
        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESTAURANT_NOT_FOUND);
    }

    @Test
    @DisplayName("해당 날짜에 식단 데이터가 없을 경우 예외를 발생시킨다")
    void execute_ThrowsException_WhenMealNotFound() {
        // given
        String dormName = "기숙사";
        String time = "내일";
        String fixedDate = "2025-05-16";

        given(buildingMapper.findBuildingNumberOfBuildingName(dormName)).willReturn(303);
        dateResolverMock.when(() -> DateResolver.resolve(time)).thenReturn(fixedDate);

        given(mealMapper.findMealsByDates(anyInt(), eq(fixedDate), anyString()))
                .willReturn(Collections.emptyList());

        RasaResponse rasaRequest = createRasaResponse(dormName, time, "ALL");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> mealStrategy.handle(rasaRequest));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEAL_NOT_FOUND);
    }

    private RasaResponse createRasaResponse(String dorm, String time, String mealType) {
        RasaResponse.Entity dormEntity = new RasaResponse.Entity();
        org.springframework.test.util.ReflectionTestUtils.setField(dormEntity, "entity", "dorm");
        org.springframework.test.util.ReflectionTestUtils.setField(dormEntity, "value", dorm);

        RasaResponse.Entity timeEntity = new RasaResponse.Entity();
        org.springframework.test.util.ReflectionTestUtils.setField(timeEntity, "entity", "time");
        org.springframework.test.util.ReflectionTestUtils.setField(timeEntity, "value", time);

        RasaResponse.Entity typeEntity = new RasaResponse.Entity();
        org.springframework.test.util.ReflectionTestUtils.setField(typeEntity, "entity", "meal_type");
        org.springframework.test.util.ReflectionTestUtils.setField(typeEntity, "value", mealType);

        RasaResponse rasaResponse = new RasaResponse();
        org.springframework.test.util.ReflectionTestUtils.setField(rasaResponse, "entities", List.of(dormEntity, timeEntity, typeEntity));
        return rasaResponse;
    }
}