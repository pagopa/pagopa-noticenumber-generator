package it.gov.pagopa.noticenumber.service;

import it.gov.pagopa.noticenumber.config.NoticeNumberProperties;
import it.gov.pagopa.noticenumber.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.noticenumber.exception.AppException;
import it.gov.pagopa.noticenumber.model.response.IUVGenerationResponse;
import it.gov.pagopa.noticenumber.service.algorithm.AuxDigitIUVGeneratorAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeNumberGeneratorService {

    private final StringRedisTemplate redisTemplate;
    private final NoticeNumberProperties properties;

    public IUVGenerationResponse generateIUV(String organizationFiscalCode) {
        String iuv = null;
        int retries = 1;
        boolean found = false;

        AuxDigitIUVGeneratorAlgorithm algorithm = AuxDigitIUVGeneratorAlgorithm.getInstance()
                .auxDigit(properties.getAuxDigit())
                .segregationCode(properties.getSegregationCode());

        while (mustTryAgain(found, retries)) {
            log.debug("Generating IUV for organization {}. Retry: {}", organizationFiscalCode, retries);
            iuv = algorithm.generate();
            found = this.checkIUVUniqueness(organizationFiscalCode, iuv);
            retries++;
        }

        if (!found) {
            log.error("Impossible to generate a valid unique IUV in [{}] retries for organization [{}].", retries - 1, organizationFiscalCode);
            throw new AppException(AppErrorCodeMessageEnum.GENERATION_MAX_RETRIES_REACHED, retries - 1, organizationFiscalCode);
        }

        return IUVGenerationResponse.builder()
                .iuv(iuv)
                .build();
    }

    private boolean mustTryAgain(boolean found, int retries) {
        return !found && retries <= properties.getMaxRetries();
    }

    private boolean checkIUVUniqueness(String organizationFiscalCode, String iuv) {
        String redisKey = String.format("%slock:%s:%s",
                properties.getRedisKeyPrefix(),
                organizationFiscalCode,
                iuv);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey, "USED", properties.getLockTtl()));
    }
}
