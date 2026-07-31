package it.gov.pagopa.noticenumber.client;

import com.microsoft.applicationinsights.TelemetryClient;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.noticenumber.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
        // Creiamo l'istanza reale passando la stringa dummy al costruttore
        appInsightTelemetryClient = new AppInsightTelemetryClient("InstrumentationKey=dummy-key");
        // Sostituiamo il TelemetryClient interno con il nostro Mock
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

        // When
        appInsightTelemetryClient.createCustomEventForAlert(errorCode, details, exception);

        // Then
        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_ShouldTrackEventSuccessfullyWithGenericException() {
        // Given
        AppErrorCodeMessageEnum errorCode = AppErrorCodeMessageEnum.GENERATION_MAX_RETRIES_REACHED;
        String details = "Generic internal failure";
        Exception exception = new RuntimeException("Generic error");

        // When
        appInsightTelemetryClient.createCustomEventForAlert(errorCode, details, exception);

        // Then
        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_WithNestedCauseException() {
        // Given
        Exception cause = new RuntimeException("Nested database timeout exception");
        Exception mainException = new RuntimeException("Main process failed", cause);

        // When
        appInsightTelemetryClient.createCustomEventForAlert(
                AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN,
                "Testing with nested cause",
                mainException
        );

        // Then
        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }

    @Test
    void createCustomEventForAlert_WithNullException() {
        // When
        appInsightTelemetryClient.createCustomEventForAlert(
                AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN,
                "Testing with null exception",
                null
        );

        // Then
        verify(telemetryClientMock, times(1)).trackEvent(anyString(), any(Map.class), any());
    }
}