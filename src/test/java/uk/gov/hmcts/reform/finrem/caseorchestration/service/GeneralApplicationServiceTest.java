package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsBeforeFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
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

    @Autowired
    private GeneralApplicationService generalApplicationService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IdamService idamService;

    @Test
    public void updateCaseDataSubmit() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/general-application.json", objectMapper);
        CaseDetails caseDetailsBefore = caseDetailsBeforeFromResource("/fixtures/general-application.json", objectMapper);
        String authToken = "token";

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, authToken);

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
        String authToken = "token";

        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore, authToken);

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
        String name = "name";
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(name);

        generalApplicationService.updateCaseDataStart(caseDetails.getData(), AUTH_TOKEN);

        Stream.of(GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT)
            .forEach(ccdFieldName -> assertThat(caseDetails.getData().get(ccdFieldName), is(nullValue())));
        assertThat(caseDetails.getData().get(GENERAL_APPLICATION_CREATED_BY), is(name));
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
