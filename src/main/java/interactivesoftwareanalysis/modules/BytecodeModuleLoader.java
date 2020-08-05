package interactivesoftwareanalysis.modules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads modules from class files and instantiates them.
 */
public class BytecodeModuleLoader implements ModuleLoader {
    @Override
    public List<Module> loadModules(ModuleContext moduleContext) {
        List<Module> modules = new ArrayList<>();
        Path moduleFolderPath = FileSystems.getDefault().getPath("modules", "bytecode");
        try {
            // create the directories, if they don't exist, so users can put .class file there
            Files.createDirectories(moduleFolderPath);

            // set up a class loader, so the module classe can be loaded and instantiated
            URL[] urls = new URL[]{moduleFolderPath.toAbsolutePath().normalize().toUri().toURL()};
            URLClassLoader cl = URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());

            // walk the file tree and try to instantiate classes in .class files
            Files.walkFileTree(moduleFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isClassFile(file)) {
                        try {
                            Class<?> c = cl.loadClass(pathToClassName(moduleFolderPath.relativize(file)));
                            Module module = Module.class.cast(c.getConstructor(ModuleContext.class).newInstance(moduleContext));
                            modules.add(module);
                        } catch (ClassCastException | IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modules;
    }

    /**
     * Determines weather the given path can be a java class file.
     * The file contents are not checked.
     * @param path the path to the file to be checked
     * @return true iff the path belongs to a .class file and is not an inner class ($ character).
     */
    private boolean isClassFile(Path path) {
        return path.toFile().isFile() && path.toString().endsWith(".class") && !path.toString().contains("$");
    }

    /**
     * Converts a relative path to a class file to a class name.
     * example:
     * <code>
     *     "com/example/Test.class" -> "com.example.Test"
     * </code>
     * @param path the relative path to the class file, beginning at the package root.
     * @return a fully qualified class name for the class file
     */
    private String pathToClassName(Path path) {
        String pathString = path.toString();
        return pathString.substring(0, pathString.length() - 6).replace(File.separatorChar, '.');
    }
}
