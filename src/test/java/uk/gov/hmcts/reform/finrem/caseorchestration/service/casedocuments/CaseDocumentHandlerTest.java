package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;

public abstract class CaseDocumentHandlerTest {

    protected CaseDetails caseDetails;
    protected Map<String, Object> caseData;
    protected final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    protected final ObjectMapper mapper = new ObjectMapper();


    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    protected ContestedUploadedDocumentData createContestedUploadDocumentItem(String type, String party,
                                                                              String isConfidential, String isFdr, String other) {
        UUID uuid = UUID.randomUUID();

        return ContestedUploadedDocumentData.builder()
            .id(uuid.toString())
            .uploadedCaseDocument(ContestedUploadedDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    }

    protected List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.registerModule(new JavaTimeModule()).convertValue(data.get(field),
            new TypeReference<>() {
            });
    }
}
