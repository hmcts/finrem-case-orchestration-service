package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class NotificationTests extends IntegrationTestBase {

    @Value("${cos.notification.judge-assign.api}")
    private String notifyAssignToJudge ;

    @Value("${cos.notification.consent-order-available.api}")
    private String consentOrderAvailable ;

    @Value("${cos.notification.consent-order-approved.api}")
    private String consentOrderMade;

    @Value("${cos.notification.consent-order-unapproved.api}")
    private String consentOrderNotApproved;

    @Value("${cos.notification.hwf-success.api}")
    private String hwfSuccessfulApiUri;


    //Resource URL null
    @Test
    public void verifyNotifyAssignToJudgeTestIsOkay() {

        validatePostSuccessForNotification(notifyAssignToJudge, "ccd-request-with-solicitor-assignToJudge.json");

    }

    //400
    @Test
    public void verifyNotifyConsentOrderAvailableTestIsOkay() {

        validatePostSuccessForNotification(consentOrderAvailable,
                "ccd-request-with-solicitor-consentOrderAvailable.json");

    }

    //400
    @Test
    public void verifyNotifyConsentOrderMadeTestIsOkay() {

        validatePostSuccessForNotification(consentOrderMade, "ccd-request-with-solicitor-consentOrderMade.json");

    }

    //ResourceURL Null
    @Test
    public void verifyNotifyConsentOrderNotApprovedTestIsOkay() {

        validatePostSuccessForNotification(consentOrderNotApproved,
                "ccd-request-with-solicitor-consentOrderNotApproved.json");

    }


    //400
    @Test
    public void verifyNotifyHwfSuccessfulTestIsOkay() {

        validatePostSuccessForNotification(hwfSuccessfulApiUri, "ccd-request-with-solicitor-hwfSuccessfulEmail.json");

    }

    private void validatePostSuccessForNotification(String url, String jsonFileName) {
        System.out.println("Test URL is right and Request Data : " + url);
        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");

        System.out.println(utils.getJsonFromFile(jsonFileName));

        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");



        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getNewHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then().assertThat().statusCode(204);
    }

}
