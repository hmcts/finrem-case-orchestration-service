package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class NotificationTests extends IntegrationTestBase {

    @Value("${cos.notification.prepare-for-hearing.api}")
    private String prepareForHearingApiUri;

    @Value("${cos.notification.prepare-for-hearing-order-sent.api}")
    private String prepareForHearingOrderSentApiUri;

    @Value("${cos.notification.contest-application-issued.api}")
    private String contestApplicationIssuedApiUri;

    @Value(" /notify/update-frc")
    private String updateFrcInfoUri;

    private final String consentedDir = "/json/consented/";
    private final String contestedDir = "/json/contested/";

    @Test
    public void verifyNotifyPrepareForHearingTestIsOkay() {

        utils.validatePostSuccess(prepareForHearingApiUri,
            "ccd-request-with-solicitor-prepareForHearing.json", contestedDir);
    }

    @Test
    public void verifyNotifyContestApplicationIssuedIsOkay() {

        utils.validatePostSuccess(contestApplicationIssuedApiUri,
            "ccd-request-with-solicitor-contestApplicationIssued.json", contestedDir);
    }

    @Ignore
    @Test
    public void verifyNotifyUpdateFrcInfoIsOkay() {
        utils.validatePostSuccess(updateFrcInfoUri, "update-frc-info.json", contestedDir);
    }
}
