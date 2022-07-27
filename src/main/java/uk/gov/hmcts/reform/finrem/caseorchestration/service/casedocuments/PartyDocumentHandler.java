package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentHandler extends CaseDocumentHandler<UploadCaseDocumentCollection> {

    protected static final String APPLICANT = "applicant";
    protected static final String RESPONDENT = "respondent";

    private final String party;

    public PartyDocumentHandler(String party) {
        this.party = party;
    }

    public void handle(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData) {
        List<UploadCaseDocumentCollection> documentsFiltered = uploadedDocuments.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getValue();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentParty() != null
                    && CaseDocumentParty.forValue(party).equals(uploadedCaseDocument.getCaseDocumentParty());
            })
            .filter(d -> d.getValue().getCaseDocumentType() != null
                && isDocumentTypeValid(d.getValue().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<UploadCaseDocumentCollection> documentCollection = getDocumentCollection(caseData);
        documentCollection.addAll(documentsFiltered);
        log.info("Adding items: {}, to {} Collection", documentsFiltered,
            getClass().getName().replaceAll("Handler", ""));
        uploadedDocuments.removeAll(documentsFiltered);

        if (!documentCollection.isEmpty()) {
            setDocumentCollection(caseData, documentCollection);
        }
    }

    protected abstract boolean isDocumentTypeValid(String caseDocumentType);

    protected abstract boolean isDocumentTypeValid(CaseDocumentType caseDocumentType);

    protected abstract List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData);

    protected abstract void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs);
}
