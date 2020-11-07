package ru.nanit.vasyascheduler.api.util;

import org.apache.commons.io.FilenameUtils;
import ru.nanit.vasyascheduler.VasyaScheduler;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class LibLoader {

    public LibLoader(){}

    /**
     * Load jar file into a classpath
     * @param jar Full path to jar file
     */
    public static void loadJar(Path jar) {
        if(!FilenameUtils.getExtension(jar.toFile().getName()).equalsIgnoreCase("jar")){
            Logger.error("Attempt to load non-jar file into a classpath. Attempt ignored");
            return;
        }

        try{
            ClassLoader cl = VasyaScheduler.class.getClassLoader();

            Class<?> clazz = cl.getClass();
            Method method = clazz.getSuperclass().getDeclaredMethod("addURL", URL.class);

            method.setAccessible(true);
            method.invoke(cl, jar.toUri().toURL());
        } catch (Exception e){
            e.printStackTrace();
            Logger.error("Library " + jar.getFileName() + " not loaded: " + e.getMessage());
        }
    }

    /**
     * Load all jar files from collection
     * @param files Iterable collection of files
     */
    public static void load(Iterable<Path> files) {
        files.forEach(LibLoader::loadJar);
    }

    /**
     * Load all jar files from stream
     * @param files Stream of files
     */
    public static void load(Stream<Path> files) {
        Stream<Path> filtered = filter(files);
        filtered.forEach(LibLoader::loadJar);
    }

    /**
     * Load all jar files from directory
     * @param dir Directory that contains jar files
     */
    public static void load(Path dir) throws Exception {
        Stream<Path> libs = Files.list(dir);
        load(libs);
    }

    private static Stream filter(Stream<Path> stream){
        return stream.filter((file)->FilenameUtils.getExtension(file.toFile().getName()).equalsIgnoreCase("jar"));
    }
}
