package dev.thomazz.spring.bukkit;

import co.aikar.commands.PaperCommandManager;
import dev.thomazz.spring.bukkit.util.MultiClassLoader;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class SpringBukkitJavaPlugin extends JavaPlugin {
    private PaperCommandManager commandManager;
    private AnnotationConfigApplicationContext context;

    @Override
    public void onEnable() {
        this.commandManager = new PaperCommandManager(this);

        // Use a multi class loader to look for plugin classes
        Class<? extends SpringBukkitJavaPlugin> type = this.getClass();
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
            new MultiClassLoader(type.getClassLoader(), defaultClassLoader)
        );

        try {
            this.context = new AnnotationConfigApplicationContext();
            this.context.register(SpringBukkit.class);
            this.context.getBeanFactory().registerSingleton(PaperCommandManager.class.getName(), this.commandManager);
            this.context.getBeanFactory().registerSingleton(Plugin.class.getName(), this);
            this.context.refresh();
        } finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }

        this.getLogger().info("Finished setting up application context!");
    }

    @Override
    public void onDisable() {
        this.context.close();
    }
}
