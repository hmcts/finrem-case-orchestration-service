package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentsHolder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgreedPensionSharingAnnexCollection implements UploadingDocumentsHolder<AgreedPensionSharingAnnex> {

    private AgreedPensionSharingAnnex value;
}
