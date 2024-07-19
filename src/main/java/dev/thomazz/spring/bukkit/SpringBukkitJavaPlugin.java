package dev.thomazz.spring.bukkit;

import co.aikar.commands.PaperCommandManager;
import dev.thomazz.spring.bukkit.util.MultiClassLoader;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Getter
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
            this.registerBeanTypeAgnostic(type, this);
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

    @SuppressWarnings("unchecked")
    private <T extends SpringBukkitJavaPlugin> void registerBeanTypeAgnostic(Class<T> type, Object plugin) {
        this.context.registerBean(type, () -> (T) plugin);
    }
}
