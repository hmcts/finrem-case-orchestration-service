package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenDocumentWrapper {

    private List<CitizenDocumentCollectionItem> citizenApplicantDocument;
    private List<CitizenDocumentCollectionItem> citizenRespondentDocument;

    private List<CitizenDocumentCollectionItem> applicantTabDocs;
    private List<CitizenDocumentCollectionItem> respondentTabDocs;
    private List<CitizenDocumentCollectionItem> fdrTabDocs;
    // WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT
    private List<CitizenDocumentCollectionItem> divorceSolutionTabDocs;
    // DIVORCE_APPLICATION_PETITION
    // DIVORCE_CONDITIONAL_ORDER_DECREE_NISI
    // DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE
    private List<CitizenDocumentCollectionItem> hearingBundleTabDocs;
    // HEARING_BUNDLE
    // FDR_BUNDLE
}
