package dev.thomazz.spring.bukkit.configuration;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface PluginConfiguration {
    String YML_SUFFIX = ".yml";

    @AliasFor(annotation = Component.class)
    String value() default "config";
    boolean autoSave() default true;
}
