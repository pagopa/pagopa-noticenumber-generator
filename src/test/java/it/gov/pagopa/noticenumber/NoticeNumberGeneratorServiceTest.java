package it.gov.pagopa.noticenumber;

import it.gov.pagopa.noticenumber.config.NoticeNumberProperties;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NoticeNumberGeneratorServiceTest {

    @InjectMocks
    private NoticeNumberGeneratorService noticeNumberGeneratorService;

    @Mock
    private NoticeNumberProperties properties;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getAuxDigit()).thenReturn(3);
        lenient().when(properties.getSegregationCode()).thenReturn(12);
        lenient().when(properties.getMaxRetries()).thenReturn(5);
        lenient().when(properties.getRedisKeyPrefix()).thenReturn("test:");
        lenient().when(properties.getLockTtl()).thenReturn(Duration.ofMinutes(2));

        ValueOperations<String, String> valueOperationsMock = mock(ValueOperations.class);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperationsMock);

        lenient().when(valueOperationsMock.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        lenient().when(valueOperationsMock.setIfAbsent(anyString(), anyString(), any(Long.class), any(TimeUnit.class)))
                .thenReturn(true);

        lenient().when(valueOperationsMock.setIfAbsent(anyString(), anyString()))
                .thenReturn(true);
    }

    @Test
    void shouldGenerateCompleteValidNavStructure() {
        // Given
        String organizationFiscalCode = "12345678901";

        // When
        NoticeNumberGenerationResponse iuvGenerationResponse = noticeNumberGeneratorService.generateNoticeNumber(organizationFiscalCode);
        String completeNav = iuvGenerationResponse.getNav();

        // Then
        assertThat(completeNav)
                .isNotNull()
                .hasSize(18)
                .containsOnlyDigits();

        String auxDigitSubstring = completeNav.substring(0, 1);
        assertThat(auxDigitSubstring).isEqualTo("3");

        String segregationSubstring = completeNav.substring(1, 3);
        assertThat(segregationSubstring).isEqualTo("12");

        String iuvPart = completeNav.substring(1);
        assertThat(iuvPart).hasSize(17);

        String checkDigitPart = completeNav.substring(16, 18);
        assertThat(checkDigitPart).matches("\\d{2}");

        log.info("Generated successfully complete NAV: {}", completeNav);
    }
}