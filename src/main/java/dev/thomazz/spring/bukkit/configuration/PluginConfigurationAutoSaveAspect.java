package dev.thomazz.spring.bukkit.configuration;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

@Aspect
@Component
public class PluginConfigurationAutoSaveAspect {
    private final Plugin plugin;
    private final ConfigurableApplicationContext context;

    @Autowired
    public PluginConfigurationAutoSaveAspect(Plugin plugin, ConfigurableApplicationContext context) {
        this.plugin = plugin;
        this.context = context;
        this.plugin.getLogger().info("Registered plugin configuration auto save aspect");
    }

    @Pointcut("@target(dev.thomazz.spring.bukkit.configuration.PluginConfiguration)")
    public void targetPluginConfigurationAnnotation() {}

    @Pointcut("execution(public * *.set*(..))")
    public void publicSetterMethods() {}

    @After("targetPluginConfigurationAnnotation() && publicSetterMethods()")
    public void afterConfigurationPublicMethodsWithArguments(JoinPoint joinPoint) {
        Object bean = joinPoint.getTarget();
        PluginConfiguration annotation = bean.getClass().getAnnotation(PluginConfiguration.class);

        // Check if auto saving is enabled first
        if (!annotation.autoSave()) {
            return;
        }

        String configFileName = annotation.value() + PluginConfiguration.YML_SUFFIX;
        this.plugin.getLogger().info("Saving to configuration: " + configFileName);

        // Get file configuration bean
        String beanName = annotation.value() + FileConfiguration.class.getSimpleName();
        FileConfiguration configuration = this.context.getBean(beanName, FileConfiguration.class);

        // Set all fields
        for (Field declaredField : bean.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            String fieldName = declaredField.getName();

            try {
                Object object = declaredField.get(bean);
                configuration.set(declaredField.getName(), object);
            } catch (Exception exception) {
                this.plugin.getLogger().warning(
                    String.format(
                        "Could not save value '%s' to file configuration '%s'",
                        fieldName,
                        configuration.getName()
                    )
                );

                exception.printStackTrace();
            }
        }

        // Save configuration to file
        File file = new File(this.plugin.getDataFolder(), configFileName);

        try {
            configuration.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().warning(
                String.format(
                    "Could save configuration %s to file %s",
                    configuration.getName(),
                    file.getAbsoluteFile()
                )
            );

            exception.printStackTrace();
        }
    }
}
