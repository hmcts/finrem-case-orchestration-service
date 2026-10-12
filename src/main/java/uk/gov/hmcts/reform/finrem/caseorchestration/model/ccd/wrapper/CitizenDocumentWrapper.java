package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;

import java.util.ArrayList;
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

    private List<CitizenDocumentCollectionItem> applicantDocCollection;
    private List<CitizenDocumentCollectionItem> respondentDocCollection;
    private List<CitizenDocumentCollectionItem> fdrDocCollection;
    private List<CitizenDocumentCollectionItem> divorceSolutionDocCollection;
    private List<CitizenDocumentCollectionItem> hearingBundleDocCollection;

    public List<CitizenDocumentCollectionItem> getApplicantDocCollection() {
        if (applicantDocCollection == null) {
            applicantDocCollection = new ArrayList<>();
        }
        return applicantDocCollection;
    }

    public List<CitizenDocumentCollectionItem> getRespondentDocCollection() {
        if (respondentDocCollection == null) {
            respondentDocCollection = new ArrayList<>();
        }
        return respondentDocCollection;
    }

    public List<CitizenDocumentCollectionItem> getFdrDocCollection() {
        if (fdrDocCollection == null) {
            fdrDocCollection = new ArrayList<>();
        }
        return fdrDocCollection;
    }

    public List<CitizenDocumentCollectionItem> getDivorceSolutionDocCollection() {
        if (divorceSolutionDocCollection == null) {
            divorceSolutionDocCollection = new ArrayList<>();
        }
        return divorceSolutionDocCollection;
    }

    public List<CitizenDocumentCollectionItem> getHearingBundleDocCollection() {
        if (hearingBundleDocCollection == null) {
            hearingBundleDocCollection = new ArrayList<>();
        }
        return hearingBundleDocCollection;
    }
}
