package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class InterfaceImplementingClasses {

    public static List<Class<?>> findClassesImplementingInterface(Class<?> interfaceType, String packageName) {
        List<Class<?>> implementingClasses = new ArrayList<>();
        try {
            Class<?>[] classes = getClassesInPackage(packageName);

            for (Class<?> clazz : classes) {
                if (interfaceType.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    implementingClasses.add(clazz);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return implementingClasses;
    }

    public static Class<?>[] getClassesInPackage(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes.toArray(new Class[0]);
    }

    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}

