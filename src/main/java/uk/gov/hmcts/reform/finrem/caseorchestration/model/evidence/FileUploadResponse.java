package uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.FinremDateUtils.getLocalDateTime;

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

    public LocalDateTime getModifiedOn() {
        return getLocalDateTime(this.modifiedOn);
    }

    public LocalDateTime getCreatedOn() {
        return getLocalDateTime(this.createdOn);
    }
}
