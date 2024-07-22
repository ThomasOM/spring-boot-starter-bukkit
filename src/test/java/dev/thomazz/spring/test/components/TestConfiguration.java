package dev.thomazz.spring.test.components;

import dev.thomazz.spring.bukkit.configuration.PluginConfiguration;
import lombok.Data;

@Data
@PluginConfiguration("test")
public class TestConfiguration {
    private String value1;
    private int value2;
    private double value3;
}
