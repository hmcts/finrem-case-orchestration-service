package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

public class ContestedOrderApprovedLetterServiceTest extends BaseServiceTest {

    @Autowired private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Autowired private DocumentConfiguration documentConfiguration;

    @MockBean private GenericDocumentService genericDocumentService;

    @Captor private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @Test
    public void whenContestedApprovedOrderLetterGenerated_thenTemplateVarsPopulatedAndDocumentCreatedAndStoredInCaseDetails() {
        CaseDocument expectedCaseDocument = caseDocument();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(expectedCaseDocument);

        CaseDetails caseDetails = testCaseDetails();
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedOrderApprovedCoverLetterTemplate()),
            eq(documentConfiguration.getContestedOrderApprovedCoverLetterFileName()));

        verifyTemplateVariablesArePopulated();
        assertThat(caseDetails.getData().get(CONTESTED_ORDER_APPROVED_COVER_LETTER), is(expectedCaseDocument));
    }

    private CaseDetails testCaseDetails() {
        CaseDetails caseDetails = defaultContestedCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Contested Applicant");
        caseData.put(APPLICANT_LAST_NAME, "Name");
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Contested Respondent");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Name");
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_TYPE, "Her Honour");
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, "Judge Contested");

        return caseDetails;
    }

    private void verifyTemplateVariablesArePopulated() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("Court"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
        System.out.println(data.get("letterDate"));
        assertThat(data.get("letterDate"), is(LocalDate.now()));
    }
}
