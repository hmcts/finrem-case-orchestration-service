package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionMidlandsFrc;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class ContestedOrderApprovedLetterServiceTest extends BaseServiceTest {

    @Autowired private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Autowired private DocumentConfiguration documentConfiguration;

    @MockBean private GenericDocumentService genericDocumentService;

    @Captor private ArgumentCaptor<Map<String, Object>> placeholdersMapArgumentCaptor;

    @Test
    public void whenContestedApprovedOrderLetterGenerated_thenTemplateVarsPopulatedAndDocumentCreatedAndStoredInCaseDetails() {
        Document expectedCaseDocument = newDocument();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(expectedCaseDocument);

        FinremCaseDetails caseDetails = testCaseDetails();
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedOrderApprovedCoverLetterTemplate()),
            eq(documentConfiguration.getContestedOrderApprovedCoverLetterFileName()));

        verifyTemplateVariablesArePopulated();
        assertThat(caseDetails.getCaseData().getOrderApprovedCoverLetter(), is(expectedCaseDocument));
    }

    private FinremCaseDetails testCaseDetails() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        caseData.getContactDetailsWrapper().setApplicantFmName("Contested Applicant");
        caseData.getContactDetailsWrapper().setApplicantLname("Name");
        caseData.getContactDetailsWrapper().setRespondentFmName("Contested Respondent");
        caseData.getContactDetailsWrapper().setRespondentLname("Name");
        caseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getDefaultRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setOrderApprovedJudgeType(JudgeType.HER_HONOUR_JUDGE);
        caseData.setOrderApprovedJudgeName("Contested");

        return caseDetails;
    }

    private void verifyTemplateVariablesArePopulated() {
        Map<String, Object> data = placeholdersMapArgumentCaptor.getValue();
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("Court"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
        assertThat(data.get("letterDate"), is(LocalDate.now()));
    }
}
