package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
abstract public class PartyDocumentHandler extends DocumentHandler {

    private final String collectionName;
    private final String party;
    private final ObjectMapper mapper;

    protected static final String APPLICANT = "applicant";
    protected static final String RESPONDENT = "respondent";

    public PartyDocumentHandler(String collectionName, String party, ObjectMapper mapper) {
        super(mapper);
        this.collectionName = collectionName;
        this.party = party;
        this.mapper = mapper;
    }

    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments,
                       Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> documentsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isDocumentTypeValid(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> documentCollection = getDocumentCollection(caseData, collectionName);
        documentCollection.addAll(documentsFiltered);
        log.info("Adding items: {}, to {} Collection", documentsFiltered, collectionName);
        uploadedDocuments.removeAll(documentsFiltered);

        if (!documentCollection.isEmpty()) {
            caseData.put(collectionName, documentCollection);
        }
    }

    abstract protected boolean isDocumentTypeValid(String caseDocumentType);
}
