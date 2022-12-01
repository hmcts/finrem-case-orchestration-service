package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentManagerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantCaseSummariesHandlerTest extends CaseDocumentManagerTest {

    private CaseDetails caseDetails;
    private Map<String, Object> caseData;
    private final List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    ApplicantCaseSummariesManager applicantCaseSummariesHandler = new ApplicantCaseSummariesManager(mapper);

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantCaseSummariesHandler.manageDocumentCollection(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_CASE_SUMMARIES_COLLECTION), hasSize(3));
    }

}