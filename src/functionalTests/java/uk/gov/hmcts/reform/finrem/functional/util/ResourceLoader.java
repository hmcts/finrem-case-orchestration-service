package uk.gov.hmcts.reform.finrem.functional.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceLoader {

    public static String loadResourceAsString(final String filePath) throws Exception {
        URL url = ResourceLoader.class.getClassLoader().getResource(filePath);

        if (url == null) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        return new String(Files.readAllBytes(Paths.get(url.toURI())));
    }
}
