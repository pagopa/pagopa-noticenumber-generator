package it.gov.pagopa.noticenumber.config;

import it.gov.pagopa.noticenumber.client.AppInsightTelemetryClient;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(NoticeNumberProperties.class)
public class NoticeNumberAutoConfiguration {

    @Bean
    public NoticeNumberGeneratorService noticeNumberGeneratorService(
            StringRedisTemplate stringRedisTemplate,
            NoticeNumberProperties noticeNumberProperties,
            ObjectProvider<AppInsightTelemetryClient> telemetryClientProvider
    ) {
        return new NoticeNumberGeneratorService(stringRedisTemplate, noticeNumberProperties, telemetryClientProvider);
    }
}
