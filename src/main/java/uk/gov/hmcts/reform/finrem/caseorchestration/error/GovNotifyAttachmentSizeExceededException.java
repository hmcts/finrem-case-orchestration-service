package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import lombok.Getter;

@Getter
public class GovNotifyAttachmentSizeExceededException extends RuntimeException {

    private final int exceedFileSizeInMb;

    public GovNotifyAttachmentSizeExceededException(int exceedFileSizeInMb) {
        super("File is larger than " + exceedFileSizeInMb + "MB");
        this.exceedFileSizeInMb = exceedFileSizeInMb;
    }

}
