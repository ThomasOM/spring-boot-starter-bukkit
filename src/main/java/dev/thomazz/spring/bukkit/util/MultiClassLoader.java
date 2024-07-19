package dev.thomazz.spring.bukkit.util;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MultiClassLoader extends ClassLoader {
    private final Collection<ClassLoader> classLoaders;

    public MultiClassLoader(ClassLoader... loaders) {
        this(Arrays.asList(loaders));
    }

    @Override
    public URL getResource(String name) {
        return this.classLoaders.stream()
            .map(loader -> loader.getResource(name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return this.classLoaders.stream()
            .map(loader -> loader.getResourceAsStream(name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Enumeration<URL> getResources(String name)  {
        List<URL> urls = this.classLoaders.stream()
            .map(loader -> this.getResourcesWrapped(loader, name))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return Collections.enumeration(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.classLoaders.stream()
            .map(loader -> this.loadClassWrapped(loader, name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(ClassNotFoundException::new);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass(name);
    }

    private List<URL> getResourcesWrapped(ClassLoader loader, String name) {
        try {
            return Collections.list(loader.getResources(name));
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
    }

    private Class<?> loadClassWrapped(ClassLoader loader, String name) {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}