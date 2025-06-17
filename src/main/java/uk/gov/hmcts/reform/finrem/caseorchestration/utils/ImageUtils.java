package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image-related operations.
 */
public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Reads an image file from the classpath and returns its contents as a byte array.
     *
     * @param fileName the name or path of the image file relative to the classpath (e.g., "/images/sample.png")
     * @return a byte array representing the image file content
     * @throws IOException           if an I/O error occurs while reading the file
     * @throws IllegalStateException if the file is not found on the classpath
     */
    public static byte[] imageAsBytes(String fileName) throws IOException {
        InputStream inputStream = ImageUtils.class.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalStateException("File not found: " + fileName);
        }
        try (InputStream is = inputStream) {
            return IOUtils.toByteArray(is);
        }
    }
}
