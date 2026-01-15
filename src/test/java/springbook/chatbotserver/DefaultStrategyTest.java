package springbook.chatbotserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import springbook.chatbotserver.chat.model.dto.RasaResponse;
import springbook.chatbotserver.chat.service.strategy.intent.DefaultStrategy;
import springbook.chatbotserver.config.exception.CustomException;
import springbook.chatbotserver.config.exception.ErrorCode;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class DefaultStrategyTest {

    @InjectMocks
    private DefaultStrategy defaultStrategy;
    @Test
    @DisplayName("처리할 수 없는 인텐트일 경우 예외를 발생시킨다")
    void execute_ThrowsCustomException_WhenIntentNotFound() {
        //given
        RasaResponse rasaResponse = new RasaResponse();

        //when
        CustomException exception = assertThrows(CustomException.class, () -> defaultStrategy.handle(rasaResponse));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTENT_NOT_FOUND);
    }

}
