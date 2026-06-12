package it.gov.pagopa.noticenumber;

import it.gov.pagopa.noticenumber.config.NoticeNumberAutoConfiguration;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeNumberAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NoticeNumberAutoConfiguration.class));

    @Test
    void shouldInitializeServiceWhenPropertiesAreProvided() {
        this.contextRunner
                .withUserConfiguration(MockRedisConfig.class)
                .withPropertyValues(
                        "notice.number.aux-digit=3",
                        "notice.number.segregation-code=12",
                        "notice.number.redis-key-prefix=test-notice:",
                        "notice.number.lock-ttl=2m"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NoticeNumberGeneratorService.class);

                    NoticeNumberGeneratorService service = context.getBean(NoticeNumberGeneratorService.class);
                    assertThat(service).isNotNull();
                });
    }

    @Test
    void shouldFailWhenPropertiesAreMissing() {
        this.contextRunner
                .withUserConfiguration(MockRedisConfig.class)
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Configuration
    static class MockRedisConfig {
        @Bean
        public StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }
}