package it.gov.pagopa.noticenumber.client;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Azure Application Insight Telemetry client */
@Service
public class AppInsightTelemetryClient {

    private final TelemetryClient telemetryClient;

    public AppInsightTelemetryClient(@Value("${applicationinsights.connectionstring}") String connectionString) {
        TelemetryConfiguration aDefault = TelemetryConfiguration.createDefault();
        aDefault.setConnectionString(connectionString);
        this.telemetryClient = new TelemetryClient(aDefault);
    }

    /**
     * Create a custom event on Application Insight with the provided information
     *
     * @param errorCode the application error code enum
     * @param details custom details or context of the failure
     * @param e exception added to the custom event
     */
    public void createCustomEventForAlert(AppErrorCodeMessageEnum errorCode, String details, Exception e) {
        String errorMessage = null;
        if (e != null) {
            errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        }

        Map<String, String> props = new HashMap<>();
        props.put("code", errorCode.getCode() != null ? errorCode.getCode().toString() : "N/A");
        props.put("type", errorCode.getTitle());
        props.put("title", errorCode.getDetail());
        props.put("details", details);
        props.put("cause", e != null ? errorMessage : "N/A");

        this.telemetryClient.trackEvent("NOTICE_NUMBER_GENERATOR", props, null);
    }
}
