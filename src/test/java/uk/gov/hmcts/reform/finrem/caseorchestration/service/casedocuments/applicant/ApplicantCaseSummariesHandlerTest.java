package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantCaseSummariesHandlerTest {

    private CaseDetails caseDetails;
    private Map<String, Object> caseData;
    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    ApplicantCaseSummariesHandler applicantCaseSummariesHandler = new ApplicantCaseSummariesHandler(mapper);

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "applicant", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantCaseSummariesHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_CASE_SUMMARIES_COLLECTION), hasSize(3));
    }


    private ContestedUploadedDocumentData createContestedUploadDocumentItem(String type, String party,
                                                                            String isConfidential, String other) {
        return ContestedUploadedDocumentData.builder()
            .uploadedCaseDocument(ContestedUploadedDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .hearingDetails(null)
                .build())
            .build();
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<>() {
            });
    }
}