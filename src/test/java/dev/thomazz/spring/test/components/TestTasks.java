package dev.thomazz.spring.test.components;

import dev.thomazz.spring.bukkit.task.PluginTask;
import org.springframework.stereotype.Component;

@Component
public class TestTasks {
    @PluginTask
    public void test() {
    }
}
