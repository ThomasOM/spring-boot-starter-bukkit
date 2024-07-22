package dev.thomazz.spring.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import jakarta.annotation.Nonnull;
import org.bukkit.plugin.Plugin;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class PluginCommandBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    private final Plugin plugin;
    private final PaperCommandManager commandManager;

    @Autowired
    public PluginCommandBeanPostProcessor(Plugin plugin, PaperCommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
        this.plugin.getLogger().info("Registered plugin command bean post processor");
    }

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginCommand.class)) {
            return bean;
        }

        if (!(bean instanceof BaseCommand)) {
            String message = "@PluginCommand component should be instance of " + BaseCommand.class.getName();
            throw new BeanCreationException(message);
        }

        this.commandManager.registerCommand((BaseCommand) bean);

        this.plugin.getLogger().info("Registered plugin command: " + beanName);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginCommand.class)) {
            return;
        }

        if (!(bean instanceof BaseCommand)) {
            String message = "@PluginCommand component should be instance of " + BaseCommand.class.getName();
            throw new FatalBeanException(message);
        }

        this.commandManager.unregisterCommand((BaseCommand) bean);

        this.plugin.getLogger().info("Unregistered plugin command: " + beanName);
    }
}
