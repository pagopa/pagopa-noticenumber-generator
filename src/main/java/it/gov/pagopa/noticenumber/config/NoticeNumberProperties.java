package it.gov.pagopa.noticenumber.config;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;



@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "notice.number")
public class NoticeNumberProperties {

    @NotNull(message = "The parameter notice.number.aux-digit is mandatory")
    @Min(value = 0, message = "The auxDigit must be a single digit between 0 and 3")
    @Max(value = 3, message = "The auxDigit must be a single digit between 0 and 3")
    private Integer auxDigit;

    @NotNull(message = "The parameter notice.number.segregation-code is mandatory")
    @Min(value = 0, message = "The segregationCode must be between 00 and 99")
    @Max(value = 99, message = "The segregationCode must be between 00 and 99")
    private Integer segregationCode;

    /**
     * Prefix used for Redis keys to isolate the keys of this library (namespace)
     * and prevent data collisions if the Redis instance is shared with other microservices.
     */
    @NotBlank(message = "The parameter notice.number.redis-key-prefix is mandatory")
    private String redisKeyPrefix;

    @NotNull(message = "The parameter 'notice.number.lock-millis' is mandatory")
    @Min(value = 1, message = "The lockMillis must be at least 1 millisecond")
    private Long lockMillis;

    @Min(value = 1, message = "The maxRetries must be at least 1")
    private Integer maxRetries = 5;
}
