package uk.gov.hmcts.reform.finrem.functional.assigncaseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class AssignCaseAccessTest extends IntegrationTestBase {

    @Value("${cos.aca.applicant-solicitor.api}")
    private String applicantSolicitorUrl;

    private final String contestedDir = "/json/contested/";

    @Test
    public void verifyApplicantSolicitor() throws JsonProcessingException {
        CallbackRequest callbackRequest = new ObjectMapper().readValue(
            utils.getJsonFromFile("ccd-request-with-solicitor-contestApplicationIssued.json", contestedDir), CallbackRequest.class);

        Response response = utils.getResponseData(applicantSolicitorUrl, callbackRequest);

        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
