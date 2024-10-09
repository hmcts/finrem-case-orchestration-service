package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.DraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.PensionSharingAnnexCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class AgreedDraftOrder implements HasCaseDocument {

    private List<DraftOrderCollection> draftOrderCollection;

    private List<PensionSharingAnnexCollection> pensionSharingAnnexCollection;

}
