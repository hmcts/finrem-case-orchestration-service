package uk.gov.hmcts.reform.finrem.caseorchestration.smoketest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller.CcdDataMigrationController;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.DocumentController;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.FeePaymentController;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.NotificationsController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmokeTest {
    private static final String AUTH_TOKEN = "axseeerfderersafsfasfaf";

    @Autowired
    private FeePaymentController feePaymentController;

    @Autowired
    private NotificationsController notificationsController;

    @Autowired
    private CcdDataMigrationController ccdDataMigrationController;

    @Autowired
    private DocumentController documentController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void contextLoads() {
        assertThat(feePaymentController).isNotNull();
        assertThat(notificationsController).isNotNull();
        assertThat(ccdDataMigrationController).isNotNull();
        assertThat(documentController).isNotNull();
    }

    @Test
    public void shouldSendAnEmail() throws IOException {
        CCDRequest request;
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/notify-casedata.json")) {
            request = objectMapper.readValue(resourceAsStream, CCDRequest.class);
        }
        notificationsController.sendHwfSuccessfulConfirmationEmail(request, AUTH_TOKEN);
    }
}
