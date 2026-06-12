package it.gov.pagopa.noticenumber.config;

import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(NoticeNumberProperties.class)
public class NoticeNumberAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NoticeNumberGeneratorService noticeNumberGeneratorService(
            StringRedisTemplate stringRedisTemplate,
            NoticeNumberProperties noticeNumberProperties) {
        return new NoticeNumberGeneratorService(stringRedisTemplate, noticeNumberProperties);
    }
}
