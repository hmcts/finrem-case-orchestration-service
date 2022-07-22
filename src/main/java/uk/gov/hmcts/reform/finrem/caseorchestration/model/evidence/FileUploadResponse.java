package uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The class name FileUploadResponse is to maintain consistency. It was created in Evidence Management Client
 * to represent response for uploading a file to Document Management Store. Exactly the same metadata is returned
 * when auditing a file, so it seemed reasonable to use the same class name.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private String fileUrl;
    private String fileName;
    private String mimeType;
    private String createdBy;
    private String lastModifiedBy;
    private String createdOn;
    private String modifiedOn;
    private HttpStatus status;

    public ZonedDateTime getModifiedOn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2015-05-05 10:15:30 Europe/Paris", formatter);

        return zonedDateTime;
    }
}
