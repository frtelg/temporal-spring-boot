package com.frtelg.temporal.configuration;

import com.frtelg.temporal.config.TemporalConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({TemporalTestServer.class, TemporalConfiguration.class})
public @interface EnableTemporalIntegrationTest {
}
