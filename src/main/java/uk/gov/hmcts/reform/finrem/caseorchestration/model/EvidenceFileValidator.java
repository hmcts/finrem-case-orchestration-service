package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class EvidenceFileValidator implements ConstraintValidator<EvidenceFile, MultipartFile> {

    @Value("${endpoints.fileupload.extensions}")
    private String allowedFileExtensions;

    @Value("${endpoints.fileupload.mimetypes}")
    private String allowedMimeTypes;

    @Override
    public void initialize(EvidenceFile evidenceFile) {
        
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        return validFile(file.getOriginalFilename()) && validMimeType(file.getContentType());
    }

    private boolean validMimeType(final String mimeType) {
        return StringUtils.containsIgnoreCase(allowedMimeTypes, mimeType);
    }

    private boolean validFile(final String filename) {
        return StringUtils.containsIgnoreCase(allowedFileExtensions, FilenameUtils.getExtension(filename));
    }
}
