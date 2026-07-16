package it.gov.pagopa.noticenumber.service;

import it.gov.pagopa.noticenumber.client.AppInsightTelemetryClient;
import it.gov.pagopa.noticenumber.config.NoticeNumberProperties;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.noticenumber.exception.AppException;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.algorithm.AuxDigitIUVGeneratorAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeNumberGeneratorService {

    private final StringRedisTemplate redisTemplate;
    private final NoticeNumberProperties properties;
    private final ObjectProvider<AppInsightTelemetryClient> telemetryClientProvider;

    public NoticeNumberGenerationResponse generateNoticeNumber(String organizationFiscalCode) {
        String iuv = null;
        int retries = 1;
        boolean found = false;

        AuxDigitIUVGeneratorAlgorithm algorithm = AuxDigitIUVGeneratorAlgorithm.getInstance()
                .auxDigit(properties.getAuxDigit())
                .segregationCode(properties.getSegregationCode());

        AppInsightTelemetryClient telemetryClient = telemetryClientProvider.getIfAvailable();

        while (mustTryAgain(found, retries)) {
            log.debug("Generating IUV for organization {}. Retry: {}", organizationFiscalCode, retries);
            try {
                iuv = algorithm.generate();
            }catch (AppException e) {
                if (telemetryClient != null){
                    telemetryClient.createCustomEventForAlert(
                            AppErrorCodeMessageEnum.GENERATION_AUXDIGIT_ALGORITHM_INVALID_PATTERN,
                            "Failed to generate notice number due to algorithm pattern mismatch (13 digits check failed)",
                            e
                    );
                }
                throw e;
            }

            found = this.checkIUVUniqueness(organizationFiscalCode, iuv);
            retries++;
        }

        if (!found) {
            log.error("Impossible to generate a valid unique IUV in {} retries for organization {}.", retries - 1, organizationFiscalCode);
            throw new AppException(AppErrorCodeMessageEnum.GENERATION_MAX_RETRIES_REACHED, retries - 1, organizationFiscalCode);
        }

        return NoticeNumberGenerationResponse.builder()
                .noticeNumber(properties.getAuxDigit()+iuv)
                .build();
    }

    private boolean mustTryAgain(boolean found, int retries) {
        return !found && retries <= properties.getMaxRetries();
    }

    public boolean checkIUVUniqueness(String organizationFiscalCode, String iuv) {
        String redisKey = String.format("%slock:%s:%s",
                properties.getRedisKeyPrefix(),
                organizationFiscalCode,
                iuv);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey, "USED", properties.getLockTtl()));
    }
}
