package dev.thomazz.spring.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import dev.thomazz.spring.bukkit.SpringBukkitJavaPlugin;
import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class PluginCommandBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    private final SpringBukkitJavaPlugin plugin;

    @Autowired
    public PluginCommandBeanPostProcessor(SpringBukkitJavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getLogger().info("Registered plugin command bean post processor");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @Nonnull String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(PluginCommand.class)) {
            return bean;
        }

        if (!(bean instanceof BaseCommand)) {
            String message = "@PluginCommand component should be instance of " + BaseCommand.class.getName();
            throw new BeanCreationException(message);
        }

        PaperCommandManager commandManager = this.plugin.getCommandManager();
        commandManager.registerCommand((BaseCommand) bean);
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

        PaperCommandManager commandManager = this.plugin.getCommandManager();
        commandManager.unregisterCommand((BaseCommand) bean);
    }
}
