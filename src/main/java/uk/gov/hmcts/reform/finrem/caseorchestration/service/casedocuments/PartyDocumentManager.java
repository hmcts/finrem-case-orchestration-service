package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentManager extends CaseDocumentManager<UploadCaseDocumentCollection> {

    protected static final String APPLICANT = "applicant";
    protected static final String RESPONDENT = "respondent";
    private final String collectionName;
    private final String party;
    private final ObjectMapper mapper;

    public PartyDocumentManager(String collectionName, String party, ObjectMapper mapper) {
        super(mapper);
        this.collectionName = collectionName;
        this.party = party;
        this.mapper = mapper;
    }

    public void manageDocumentCollection(List<UploadCaseDocumentCollection> uploadedDocuments,
                                         Map<String, Object> caseData) {
        List<UploadCaseDocumentCollection> documentsFiltered = uploadedDocuments.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentParty() != null
                    && uploadedCaseDocument.getCaseDocumentParty().equals(party);
            })
            .filter(d -> d.getUploadCaseDocument().getCaseDocumentType() != null
                && isDocumentTypeValid(d.getUploadCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<UploadCaseDocumentCollection> documentCollection = getDocumentCollection(caseData, collectionName);
        documentCollection.addAll(documentsFiltered);
        documentCollection.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", documentsFiltered, collectionName);
        uploadedDocuments.removeAll(documentsFiltered);

        if (!documentCollection.isEmpty()) {
            caseData.put(collectionName, documentCollection);
        }
    }

    protected abstract boolean isDocumentTypeValid(CaseDocumentType caseDocumentType);
}
