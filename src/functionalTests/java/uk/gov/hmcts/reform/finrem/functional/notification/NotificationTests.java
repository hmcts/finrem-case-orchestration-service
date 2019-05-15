package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
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


    @Test
    public void verifyNotifyAssignToJudgeTestIsOkay() {

        validatePostSuccessForNotification(notifyAssignToJudge, "ccd-request-with-solicitor-assignedToJudge1.json");

    }


    @Test
    public void verifyNotifyConsentOrderAvailableTestIsOkay() {

        validatePostSuccessForNotification(consentOrderAvailable,
                "ccd-request-with-solicitor-consentOrderAvailable1.json");

    }


    @Test
    public void verifyNotifyConsentOrderMadeTestIsOkay() {

        validatePostSuccessForNotification(consentOrderMade, "ccd-request-with-solicitor-consentOrderMade1.json");

    }


    @Test
    public void verifyNotifyConsentOrderNotApprovedTestIsOkay() {

        validatePostSuccessForNotification(consentOrderNotApproved,
                "ccd-request-with-solicitor-consentOrderNotApproved1.json");

    }

    @Test
    public void verifyNotifyHwfSuccessfulTestIsOkay() {

        validatePostSuccessForNotification(hwfSuccessfulApiUri, "ccd-request-with-solicitor-hwfSuccessfulEmail1.json");

    }

    private void validatePostSuccessForNotification(String url, String jsonFileName) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then().assertThat().statusCode(200);
    }

}
