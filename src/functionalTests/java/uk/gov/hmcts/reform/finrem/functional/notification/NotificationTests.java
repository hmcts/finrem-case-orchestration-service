package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RunWith(SerenityRunner.class)
public class NotificationTests extends IntegrationTestBase {

    @Value("${cos.notification.judge-assign.api}")
    private String notifyAssignToJudge;

    @Value("${cos.notification.consent-order-available.api}")
    private String consentOrderAvailable;

    @Value("${cos.notification.consent-order-approved.api}")
    private String consentOrderMade;

    @Value("${cos.notification.consent-order-unapproved.api}")
    private String consentOrderNotApproved;

    @Value("${cos.notification.hwf-success.api}")
    private String hwfSuccessfulApiUri;

    @Value("${cos.notification.prepare-for-hearing.api}")
    private String prepareForHearingApiUri;

    @Value("${cos.notification.prepare-for-hearing-order-sent.api}")
    private String prepareForHearingOrderSentApiUri;

    @Value("${cos.notification.contest-application-issued.api}")
    private String contestApplicationIssuedApiUri;

    @Value("${cos.notification.contest-order-approved.api}")
    private String contestOrderApprovedApiUri;

    @Value("${cos.notification.contest-draft-order.api}")
    private String contestDraftOrderApiUri;

    @Value(" /notify/update-frc")
    private String updateFrcInfoUri;

    private final String consentedDir = "/json/consented/";
    private final String contestedDir = "/json/contested/";

    @MockBean
    AssignCaseAccessService assignCaseAccessService;

    @Test
    public void verifyNotifyAssignToJudgeTestIsOkay() {

        utils.validatePostSuccess(notifyAssignToJudge,
            "ccd-request-with-solicitor-assignedToJudge1.json", consentedDir);
    }

    @Test
    public void verifyNotifyConsentOrderAvailableTestIsOkay() {

        utils.validatePostSuccess(consentOrderAvailable,
            "ccd-request-with-solicitor-consentOrderAvailable1.json", consentedDir);
    }

    @Test
    public void verifyNotifyConsentOrderMadeTestIsOkay() {

        utils.validatePostSuccess(consentOrderMade,
            "ccd-request-with-solicitor-consentOrderMade1.json", consentedDir);
    }

    @Test
    public void verifyNotifyConsentOrderNotApprovedTestIsOkay() {

        utils.validatePostSuccess(consentOrderNotApproved,
            "ccd-request-with-solicitor-consentOrderNotApproved1.json", consentedDir);
    }

    @Test
    public void verifyNotifyHwfSuccessfulTestIsOkay() {

        when(assignCaseAccessService.getUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build(),
                CaseAssignmentUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build()))
            .build());
        utils.validatePostSuccess(hwfSuccessfulApiUri,
            "ccd-request-with-solicitor-hwfSuccessfulEmail1.json", consentedDir);
    }

    @Test
    public void verifyNotifyPrepareForHearingTestIsOkay() {

        utils.validatePostSuccess(prepareForHearingApiUri,
            "ccd-request-with-solicitor-prepareForHearing.json", contestedDir);
    }

    @Test
    public void verifyNotifyPrepareForHearingOrderSentTestIsOkay() {

        utils.validatePostSuccess(prepareForHearingOrderSentApiUri,
            "ccd-request-with-solicitor-prepareForHearing.json", contestedDir);
    }

    @Test
    public void verifyNotifyContestApplicationIssuedIsOkay() {

        utils.validatePostSuccess(contestApplicationIssuedApiUri,
            "ccd-request-with-solicitor-contestApplicationIssued.json", contestedDir);
    }

    @Test
    public void verifyNotifyUpdateFrcInfoIsOkay() {
        utils.validatePostSuccess(updateFrcInfoUri, "update-frc-info.json", contestedDir);
    }
}
