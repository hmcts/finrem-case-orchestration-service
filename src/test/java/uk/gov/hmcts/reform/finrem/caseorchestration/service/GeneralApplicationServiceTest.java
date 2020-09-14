package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsBeforeFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

public class GeneralApplicationServiceTest extends BaseServiceTest {

    @Autowired private GeneralApplicationService generalApplicationService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DocumentHelper documentHelper;

    @Test
    public void updateCaseDataSubmit() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/general-application.json", objectMapper);
        CaseDetails caseDetailsBefore = caseDetailsBeforeFromResource("/fixtures/general-application.json", objectMapper);

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore);

        List<GeneralApplicationData> generalApplicationDataList =
            (List<GeneralApplicationData>) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
        CaseDocument generalApplicationLatest = (CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST);

        doCaseDocumentAssert(generalApplicationLatest);
        assertThat(generalApplicationDataList, hasSize(1));
        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE), is(LocalDate.now()));
        doCaseDocumentAssert(generalApplicationDataList.get(0).getGeneralApplication().getGeneralApplicationDocument());
        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_PRE_STATE), is("applicationIssued"));
    }

    @Test
    public void updateCaseDataSubmit_multiple() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/general-application-multiple.json", objectMapper);
        CaseDetails caseDetailsBefore = caseDetailsBeforeFromResource("/fixtures/general-application-multiple.json", objectMapper);

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore);

        List<GeneralApplicationData> generalApplicationDataList =
            (List<GeneralApplicationData>) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
        assertThat(generalApplicationDataList, hasSize(2));
        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE), is(LocalDate.now()));
        assertThat(generalApplicationDataList.get(0).getGeneralApplication().getGeneralApplicationDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee"));
        assertThat(generalApplicationDataList.get(1).getGeneralApplication().getGeneralApplicationDocument().getDocumentUrl(), is(DOC_URL));

        CaseDocument generalApplicationLatest = (CaseDocument) caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST);
        doCaseDocumentAssert(generalApplicationLatest);
        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_PRE_STATE), is("applicationIssued"));
    }

    @Test
    public void updateCaseDataStart() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/general-application-multiple.json", objectMapper);

        generalApplicationService.updateCaseDataStart(caseDetails.getData());

        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_RECEIVED_FROM), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_CREATED_BY), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_HEARING_REQUIRED), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_TIME_ESTIMATE), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_SPECIAL_MEASURES), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_DOCUMENT), is(false));
        assertThat(caseDetails.getData().containsKey(GENERAL_APPLICATION_DRAFT_ORDER), is(false));
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
