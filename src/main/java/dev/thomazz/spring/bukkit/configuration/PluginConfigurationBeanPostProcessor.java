package dev.thomazz.spring.bukkit.configuration;

import jakarta.annotation.Nonnull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Objects;

@Component
public class PluginConfigurationBeanPostProcessor implements BeanPostProcessor {
    private final Plugin plugin;
    private final ConfigurableApplicationContext context;

    @Lazy
    public PluginConfigurationBeanPostProcessor(Plugin plugin, ConfigurableApplicationContext context) {
        this.plugin = plugin;
        this.context = context;
        this.plugin.getLogger().info("Registered plugin configuration bean post processor");
    }

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginConfiguration.class)) {
            return bean;
        }

        // Get file configuration
        PluginConfiguration annotation = bean.getClass().getAnnotation(PluginConfiguration.class);
        String configName = annotation.value();
        String configFileName = configName + PluginConfiguration.YML_SUFFIX;

        File file = new File(this.plugin.getDataFolder(), configFileName);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        this.plugin.getLogger().info("Loading plugin configuration: " + configFileName);

        // Try to load defaults
        try {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(this.plugin.getResource(configFileName)));
            configuration.setDefaults(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception ex) {
            this.plugin.getLogger().warning("Did not find defaults for plugin configuration: " + configFileName);
        }

        // Register file configuration as bean
        String fileConfigBeanName = annotation.value() + FileConfiguration.class.getSimpleName();
        this.context.getBeanFactory().registerSingleton(fileConfigBeanName, configuration);

        // Load values into bean from configuration
        for (Field declaredField : bean.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            String fieldName = declaredField.getName();
            Object object = configuration.get(fieldName);

            try {
                if (object != null) {
                    declaredField.set(bean, object);
                }
            } catch (Exception exception) {
                this.plugin.getLogger().warning(
                    String.format(
                        "Could not set value '%s' from configuration '%s'",
                        fieldName,
                        configFileName
                    )
                );

                exception.printStackTrace();
            }
        }

        return bean;
    }
}
