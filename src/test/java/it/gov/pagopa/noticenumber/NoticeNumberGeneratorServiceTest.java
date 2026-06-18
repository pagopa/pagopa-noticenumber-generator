package it.gov.pagopa.noticenumber;

import it.gov.pagopa.noticenumber.config.NoticeNumberProperties;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.noticenumber.exception.AppException;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void generateNoticeNumber_Success() {
        //Given
        String orgFiscalCode = "12345678901";

        //When
        NoticeNumberGenerationResponse response = noticeNumberGeneratorService.generateNoticeNumber(orgFiscalCode);

        //Then
        assertNotNull(response);
        assertFalse(response.getNav().isEmpty());
        assertThat(response.getNav()).containsOnlyDigits();
    }

    @Test
    void generateNoticeNumber_ShouldThrowAppException_WhenMaxRetriesReached() {
        // Given
        String organizationFiscalCode = "12345678901";

        ValueOperations<String, String> valueOperationsMock = mock(ValueOperations.class);
        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperationsMock);
        Mockito.when(valueOperationsMock.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            noticeNumberGeneratorService.generateNoticeNumber(organizationFiscalCode);
        });

        assertEquals(AppErrorCodeMessageEnum.GENERATION_MAX_RETRIES_REACHED, exception.getError());
    }

    @org.junit.jupiter.api.Test
    void testPropertiesGettersAndSetters() {
        NoticeNumberProperties noticNumProps = new NoticeNumberProperties();

        noticNumProps.setLockTtl(java.time.Duration.ofMinutes(1));
        noticNumProps.setMaxRetries(3);
        noticNumProps.setRedisKeyPrefix("test:");

        org.junit.jupiter.api.Assertions.assertNotNull(noticNumProps.getLockTtl());
        org.junit.jupiter.api.Assertions.assertEquals(3, noticNumProps.getMaxRetries());
        org.junit.jupiter.api.Assertions.assertEquals("test:", noticNumProps.getRedisKeyPrefix());
    }
}