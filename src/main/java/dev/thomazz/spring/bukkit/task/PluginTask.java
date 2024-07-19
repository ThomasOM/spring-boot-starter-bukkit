package dev.thomazz.spring.bukkit.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginTask {
    String name() default "";
    long delay() default -1L;
    long interval() default -1L;
    boolean async() default false;
}
