package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import lombok.Getter;

@Getter
public class EmailAttachmentSizeExceededException extends RuntimeException {

    private final int exceedFileSizeInMb;

    public EmailAttachmentSizeExceededException(int exceedFileSizeInMb) {
        super("File is larger than " + exceedFileSizeInMb + "MB");
        this.exceedFileSizeInMb = exceedFileSizeInMb;
    }

}
