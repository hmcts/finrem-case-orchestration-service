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

    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";

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
}
