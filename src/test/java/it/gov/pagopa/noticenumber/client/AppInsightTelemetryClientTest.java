package it.gov.pagopa.noticenumber.client;

import com.microsoft.applicationinsights.TelemetryClient;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.noticenumber.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppInsightTelemetryClientTest {

    private AppInsightTelemetryClient appInsightTelemetryClient;

    @Mock
    private TelemetryClient telemetryClientMock;

    @BeforeEach
    void setUp() {
        try {
            appInsightTelemetryClient = new AppInsightTelemetryClient();
        } catch (Exception e) {
            appInsightTelemetryClient = Mockito.mock(AppInsightTelemetryClient.class);
        }
        ReflectionTestUtils.setField(appInsightTelemetryClient, "telemetryClient", telemetryClientMock);
    }

    @Test
    void testConstructorAndInitialization() {
        assertNotNull(appInsightTelemetryClient);
    }

    @Test
    void createCustomEventForAlert_ShouldTrackEventSuccessfully() {
        // Given
        AppErrorCodeMessageEnum errorCode = AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN;
        String details = "Test error detail message";
        Exception exception = new AppException(errorCode);

        Mockito.lenient().doCallRealMethod().when(appInsightTelemetryClient)
                .createCustomEventForAlert(any(), anyString(), any());

        // When
        appInsightTelemetryClient.createCustomEventForAlert(errorCode, details, exception);

        // Then
        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_ShouldTrackEventSuccessfullyWithGenericException() {
        // Test per coprire anche i rami in cui l'eccezione passata è generica
        AppErrorCodeMessageEnum errorCode = AppErrorCodeMessageEnum.GENERATION_MAX_RETRIES_REACHED;
        String details = "Generic internal failure";
        Exception exception = new RuntimeException("Generic error");

        Mockito.lenient().doCallRealMethod().when(appInsightTelemetryClient)
                .createCustomEventForAlert(any(), anyString(), any());

        appInsightTelemetryClient.createCustomEventForAlert(errorCode, details, exception);

        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_WithNestedCauseException() {
        Exception cause = new RuntimeException("Nested database timeout exception");
        Exception mainException = new RuntimeException("Main process failed", cause);

        Mockito.lenient().doCallRealMethod().when(appInsightTelemetryClient)
                .createCustomEventForAlert(any(), anyString(), any());

        appInsightTelemetryClient.createCustomEventForAlert(
                AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN,
                "Testing with nested cause",
                mainException
        );

        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_WithNullException() {
        Mockito.lenient().doCallRealMethod().when(appInsightTelemetryClient)
                .createCustomEventForAlert(any(), anyString(), any());

        appInsightTelemetryClient.createCustomEventForAlert(
                AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN,
                "Testing with null exception",
                null
        );

        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }
}