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

    /*
     * Temp dto's for citizen uploaded docs to be sorted into respective collections
     */
    private List<CitizenDocumentCollectionItem> uploadedCuiApplicantDocuments;
    private List<CitizenDocumentCollectionItem> uploadedCuiRespondentDocuments;

    private List<CitizenDocumentCollectionItem> applicantDocCollection;
    private List<CitizenDocumentCollectionItem> respondentDocCollection;

    private List<CitizenDocumentCollectionItem> fdrApplicantDocCollection;
    private List<CitizenDocumentCollectionItem> fdrRespondentDocCollection;

    private List<CitizenDocumentCollectionItem> divorceApplicantDocCollection;
    private List<CitizenDocumentCollectionItem> divorceRespondentDocCollection;

    private List<CitizenDocumentCollectionItem> hearingApplicantDocCollection;
    private List<CitizenDocumentCollectionItem> hearingRespondentDocCollection;

    public List<CitizenDocumentCollectionItem> ensureApplicantDocCollection() {
        if (applicantDocCollection == null) {
            applicantDocCollection = new ArrayList<>();
        }
        return applicantDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureRespondentDocCollection() {
        if (respondentDocCollection == null) {
            respondentDocCollection = new ArrayList<>();
        }
        return respondentDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureFdrRespondentDocCollection() {
        if (fdrRespondentDocCollection == null) {
            fdrRespondentDocCollection = new ArrayList<>();
        }
        return fdrRespondentDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureFdrApplicantDocCollection() {
        if (fdrApplicantDocCollection == null) {
            fdrApplicantDocCollection = new ArrayList<>();
        }
        return fdrApplicantDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureDivorceRespondentDocCollection() {
        if (divorceRespondentDocCollection == null) {
            divorceRespondentDocCollection = new ArrayList<>();
        }
        return divorceRespondentDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureDivorceApplicantDocCollection() {
        if (divorceApplicantDocCollection == null) {
            divorceApplicantDocCollection = new ArrayList<>();
        }
        return divorceApplicantDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureHearingApplicantDocCollection() {
        if (hearingApplicantDocCollection == null) {
            hearingApplicantDocCollection = new ArrayList<>();
        }
        return hearingApplicantDocCollection;
    }

    public List<CitizenDocumentCollectionItem> ensureHearingRespondentDocCollection() {
        if (hearingRespondentDocCollection == null) {
            hearingRespondentDocCollection = new ArrayList<>();
        }
        return hearingRespondentDocCollection;
    }
}
