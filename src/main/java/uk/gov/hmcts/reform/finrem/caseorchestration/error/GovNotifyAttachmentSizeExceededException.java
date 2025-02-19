package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class GovNotifyAttachmentSizeExceededException extends RuntimeException {

    private int exceedFileSizeInMb;

    public GovNotifyAttachmentSizeExceededException(int exceedFileSizeInMb) {
        super("File is larger than " + exceedFileSizeInMb + "MB");
        this.exceedFileSizeInMb = exceedFileSizeInMb;
    }

    public int getExceedFileSizeInMb() {
        return this.exceedFileSizeInMb;
    }
}
