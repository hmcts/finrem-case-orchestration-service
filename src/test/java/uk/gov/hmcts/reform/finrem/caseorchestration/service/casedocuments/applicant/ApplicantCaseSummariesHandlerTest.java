package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantCaseSummariesHandlerTest extends CaseDocumentHandlerTest {

    private CaseDetails caseDetails;
    private Map<String, Object> caseData;
    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();

    ApplicantCaseSummariesHandler applicantCaseSummariesHandler = new ApplicantCaseSummariesHandler(mapper);

    @Before
    public void setUp() {
        super.setUp();
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantCaseSummariesHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_CASE_SUMMARIES_COLLECTION), hasSize(3));
    }

}