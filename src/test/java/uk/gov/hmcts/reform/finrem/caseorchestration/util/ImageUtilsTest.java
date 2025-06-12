package uk.gov.hmcts.reform.finrem.caseorchestration.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.ImageUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ImageUtilsTest {

    @Test
    void shouldReturnBytesForValidImageFile() throws IOException {
        // Arrange
        String fileName = "/test-image.png"; // Make sure this exists in src/test/resources

        // Act
        byte[] result = ImageUtils.imageAsBytes(fileName);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Optional: verify the content matches what we expect
        try (InputStream inputStream = ImageUtilsTest.class.getResourceAsStream(fileName)) {
            assertNotNull(inputStream);
            byte[] expectedBytes = IOUtils.toByteArray(inputStream);
            assertArrayEquals(expectedBytes, result);
        }
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        // Arrange
        String fileName = "/nonexistent-file.png";

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ImageUtils.imageAsBytes(fileName));

        assertEquals("File not found: " + fileName, exception.getMessage());
    }
}
