package dev.thomazz.spring.bukkit.task;

import jakarta.annotation.Nonnull;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class PluginTaskBeanPostProcessor implements BeanPostProcessor {
    private final Plugin plugin;
    private final ConfigurableApplicationContext context;

    @Autowired
    public PluginTaskBeanPostProcessor(Plugin plugin, ConfigurableApplicationContext context) {
        this.plugin = plugin;
        this.context = context;
        this.plugin.getLogger().info("Registered plugin task bean post processor");
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        for (Method declaredMethod : bean.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(PluginTask.class)) {
                continue;
            }

            // Create bukkit task from method
            PluginTask annotation = declaredMethod.getAnnotation(PluginTask.class);
            BukkitTask task = this.runTask(annotation, () -> {
                try {
                    declaredMethod.invoke(bean);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Register as bean if name is set
            if (!annotation.name().isEmpty()) {
                this.context.getBeanFactory().registerSingleton(annotation.name(), task);
            }

            this.plugin.getLogger().info("Registered plugin task: " + beanName);
        }

        return bean;
    }

    private BukkitTask runTask(PluginTask annotation, Runnable runnable) {
        BukkitScheduler scheduler = this.plugin.getServer().getScheduler();

        long interval = annotation.interval();
        long delay = annotation.delay();
        boolean async = annotation.async();

        BukkitTask task;

        if (interval > 0) {
            if (async) {
                task = scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delay, interval);
            } else {
                task = scheduler.runTaskTimer(this.plugin, runnable, delay, interval);
            }
        } else if (delay > 0) {
            if (async) {
                task = scheduler.runTaskLaterAsynchronously(this.plugin, runnable, delay);
            } else {
                task = scheduler.runTaskLater(this.plugin, runnable, delay);
            }
        } else {
            if (async) {
                task = scheduler.runTaskAsynchronously(this.plugin, runnable);
            } else {
                task = scheduler.runTask(this.plugin, runnable);
            }
        }

        return task;
    }
}
