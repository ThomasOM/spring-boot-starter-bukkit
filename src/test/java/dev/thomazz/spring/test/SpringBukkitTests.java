package dev.thomazz.spring.test;

import co.aikar.commands.PaperCommandManager;
import dev.thomazz.spring.bukkit.SpringBukkit;
import dev.thomazz.spring.bukkit.configuration.PluginConfiguration;
import dev.thomazz.spring.test.components.TestCommand;
import dev.thomazz.spring.test.components.TestConfiguration;
import dev.thomazz.spring.test.components.TestListener;
import dev.thomazz.spring.test.components.TestTasks;
import org.assertj.core.api.Assertions;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringBukkitTests  {
    @Mock
    private Plugin plugin;
    @Mock
    private PaperCommandManager commandManager;

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    public void setup() {
        when(this.plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(SpringBukkit.class);
        this.context.getBeanFactory().registerSingleton(PaperCommandManager.class.getName(), this.commandManager);
        this.context.getBeanFactory().registerSingleton(Plugin.class.getName(), this.plugin);
    }

    @AfterEach
    public void teardown() {
        this.context.close();
    }

    @DisplayName("Command registration")
    @Order(1)
    @Test
    public void testCommandRegistration() {
        this.context.registerBean(TestCommand.class);
        this.context.refresh();

        verify(this.commandManager).registerCommand(any());
    }

    @DisplayName("Listener registration")
    @Order(2)
    @Test
    public void testListenerRegistration() {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(this.plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        this.context.registerBean(TestListener.class);
        this.context.refresh();

        verify(pluginManager).registerEvents(any(), eq(this.plugin));
    }

    @DisplayName("Task registration")
    @Order(3)
    @Test
    public void testTaskRegistration() {
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);

        when(this.plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        this.context.registerBean(TestTasks.class);
        this.context.refresh();

        verify(scheduler).runTask(eq(this.plugin), any(Runnable.class));
    }

    @DisplayName("Configuration registration")
    @Order(4)
    @Test
    public void testConfigurationRegistration(@TempDir Path tempDir) {
        when(this.plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(this.plugin.getResource(any())).thenAnswer(i -> SpringBukkitTests.getResource((String) i.getArguments()[0]));

        this.context.registerBean(TestConfiguration.class);
        this.context.refresh();

        TestConfiguration configuration = this.context.getBean(TestConfiguration.class);
        Assertions.assertThat(configuration.getValue1()).isEqualTo("test");
        Assertions.assertThat(configuration.getValue2()).isEqualTo(2);
        Assertions.assertThat(configuration.getValue3()).isEqualTo(1.5);
    }

    @DisplayName("Configuration auto saving")
    @Order(5)
    @Test
    public void testConfigurationAutoSaving(@TempDir Path tempDir) {
        when(this.plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(this.plugin.getResource(any())).thenAnswer(i -> SpringBukkitTests.getResource((String) i.getArguments()[0]));

        this.context.registerBean(TestConfiguration.class);
        this.context.refresh();

        PluginConfiguration annotation = TestConfiguration.class.getAnnotation(PluginConfiguration.class);
        String fileConfigBeanName = annotation.value() + FileConfiguration.class.getSimpleName();

        FileConfiguration fileConfiguration = this.context.getBeanFactory().getBean(fileConfigBeanName, FileConfiguration.class);
        FileConfiguration spyFileConfiguration = spy(fileConfiguration);

        DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) this.context.getAutowireCapableBeanFactory();
        registry.destroySingleton(fileConfigBeanName);
        registry.registerSingleton(fileConfigBeanName, spyFileConfiguration);

        TestConfiguration configuration = this.context.getBean(TestConfiguration.class);
        configuration.setValue1("test2");
        verify(spyFileConfiguration).set("value1", "test2");
    }

    private static InputStream getResource(String resource) {
        return SpringBukkitTests.class.getClassLoader().getResourceAsStream(resource);
    }
}
