package dev.thomazz.spring.bukkit.listener;

import dev.thomazz.spring.bukkit.SpringBukkitJavaPlugin;
import jakarta.annotation.Nonnull;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class PluginListenerBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    private final SpringBukkitJavaPlugin plugin;

    @Autowired
    public PluginListenerBeanPostProcessor(SpringBukkitJavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getLogger().info("Registered plugin listener bean post processor");
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginListener.class)) {
            return bean;
        }

        if (!(bean instanceof Listener)) {
            String message = "@PluginListener component should be instance of " + Listener.class.getName();
            throw new BeanCreationException(message);
        }

        PluginManager pluginManager = this.plugin.getServer().getPluginManager();
        pluginManager.registerEvents((Listener) bean, this.plugin);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginListener.class)) {
            return;
        }

        if (!(bean instanceof Listener)) {
            String message = "@PluginListener component should be instance of " + Listener.class.getName();
            throw new FatalBeanException(message);
        }

        HandlerList.unregisterAll((Listener) bean);
    }
}
