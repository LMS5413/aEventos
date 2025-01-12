/*
 *
 * This file is part of aEventos, licensed under the MIT License.
 *
 * Copyright (c) Ars3ne
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ars3ne.eventos.utils;

import com.ars3ne.eventos.aEventos;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EventoConfigFile {

    public static void create(String name) {
        File config_file = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");
        if (config_file.exists()) return;
        aEventos.getInstance().saveResource("eventos/" + name + ".yml", false);
    }

    public static YamlConfiguration get(String name) {
        File settings = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settings);
        config.set("filename", name + ".yml");

        return config;

    }

    public static boolean exists(String name) {
        if(name.contains("converted")) return false;
        File settings = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");
        return settings.exists();
    }

    public static void save(YamlConfiguration config) throws IOException {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/eventos/", filename);
        config.set("filename", null);
        config.save(file);
        config.set("filename", filename);
    }

    public static List<File> getAllFiles() {

        try {
            return Files.walk(Paths.get(aEventos.getInstance().getDataFolder() + "/eventos"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getAllResourceFiles(String resourcePath) {
        List<String> fileNames = new ArrayList<>();

        try {
            // Usa o ClassLoader para listar os arquivos do diretório
            URL resourceUrl = aEventos.getInstance().getClass().getClassLoader().getResource(resourcePath);
            if (resourceUrl == null) {
                System.err.println("Diretório de recursos não encontrado: " + resourcePath);
                return null;
            }

            if (resourceUrl.getProtocol().equals("jar")) {
                try (JarFile jarFile = new JarFile(resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!")))) {
                    jarFile.stream()
                            .filter(entry -> entry.getName().startsWith(resourcePath) && !entry.isDirectory())
                            .forEach(entry -> fileNames.add(entry.getName().replace(resourcePath + "/", "")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNames;
    }

}
