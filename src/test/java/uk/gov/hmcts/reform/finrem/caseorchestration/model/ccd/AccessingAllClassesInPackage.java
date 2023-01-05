package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessingAllClassesInPackage {

    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
            .filter(line -> line.endsWith(".class"))
            .map(line -> getClass(line, packageName))
            .collect(Collectors.toSet());
    }

    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

    public Set<Class> getWrappedClassesForClass(Class clazz) {
        Set<Class> classes = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonUnwrapped.class)) {
                classes.add(field.getType());
                getWrappedClassesForClass(field.getType()).forEach(classes::add);
            }
        }
        return classes;
    }
}
