package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

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

    @Value("${cos.notification.contested-consent-order-approved.api}")
    private String contestedConsentOrderApprovedUri;

    private String consentedDir = "/json/consented/";
    private String contestedDir = "/json/contested/";

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
    public void verifyNotifyContestOrderApprovedIsOkay() {

        utils.validatePostSuccess(contestOrderApprovedApiUri,
            "ccd-request-with-solicitor-contestOrderApproved.json", contestedDir);
    }

    @Test
    public void verifyNotifyContestDraftOrderIsOkay() {

        utils.validatePostSuccess(contestDraftOrderApiUri,
            "applicant-solicitor-to-draft-order-with-email-consent.json", contestedDir);
    }

    @Test
    public void verifyNotifyContestedConsentOrderApprovedIsOkay() {

        utils.validatePostSuccess(contestedConsentOrderApproved,
            "ccd-request-with-solicitor-contestOrderApproved.json", contestedDir);
    }
}
