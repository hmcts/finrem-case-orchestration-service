package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected UploadCaseDocumentCollection createContestedUploadDocumentItem(String type, String party,
                                                                             YesOrNo isConfidential, YesOrNo isFdr,
                                                                             String other) {
        return UploadCaseDocumentCollection.builder()
            .value(UploadCaseDocument
                .builder()
                .caseDocuments(new Document())
                .caseDocumentType(CaseDocumentType.getCaseDocumentType(type))
                .caseDocumentParty(CaseDocumentParty.forValue(party))
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .build())
            .build();
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    }

    protected List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<>() {
            });
    }
}
