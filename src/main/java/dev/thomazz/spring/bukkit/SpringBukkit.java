package dev.thomazz.spring.bukkit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan
@RequiredArgsConstructor
public class SpringBukkit {
    private final SpringBukkitJavaPlugin plugin;
    private final ConfigurableApplicationContext context;

    @PostConstruct
    public void init() {
        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        beanFactory.registerSingleton(Server.class.getCanonicalName(), Bukkit.getServer());
        beanFactory.registerSingleton(PluginManager.class.getCanonicalName(), Bukkit.getPluginManager());
        beanFactory.registerSingleton(BukkitScheduler.class.getCanonicalName(), Bukkit.getScheduler());
        this.plugin.getLogger().info("Registered bukkit singleton beans");
    }
}
