package uk.gov.hmcts.reform.finrem.functional.notification;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class NotificationTests extends IntegrationTestBase {

    @Value("${cos.notification.contest-application-issued.api}")
    private String contestApplicationIssuedApiUri;

    @Value("${cos.notification.update-frc-information.api}")
    private String updateFrcInfoUri;

    private static final String CONTESTED_DIR = "/json/contested/";

    @Test
    public void verifyNotifyContestApplicationIssuedIsOkay() {
        utils.validatePostSuccess(contestApplicationIssuedApiUri,
            "ccd-request-with-solicitor-contestApplicationIssued.json", CONTESTED_DIR);
    }

    @Test
    public void verifyNotifyUpdateFrcInfoIsOkay() {
        utils.validatePostSuccess(updateFrcInfoUri, "update-frc-info.json", CONTESTED_DIR);
    }
}
