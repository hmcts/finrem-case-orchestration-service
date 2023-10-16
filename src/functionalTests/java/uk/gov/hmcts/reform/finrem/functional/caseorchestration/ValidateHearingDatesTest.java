package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@RunWith(SerenityRunner.class)
public class ValidateHearingDatesTest extends IntegrationTestBase {

    @Value("${cos.document.hearing.api}")
    private String validateHearing;

    @Value("${cos.hearing.notification.api}")
    private String validateHearingSubmitted;

    private final String contestedDir = "/json/contested/";
    private final String consentedDir = "/json/consented/";

    @Test
    public void verifyShouldReturnBadRequestWhenCaseDataIsMissingInRequest() {
        Response response = utils.getResponse(validateHearingSubmitted, "empty-casedata1.json",consentedDir);

        assertThat(response.getStatusCode(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(response.body().asString(), startsWith("Some server side exception occurred. Please check logs for details"));
    }

    @Test
    public void verifyShouldSuccessfullyValidate() {
        assertThat(getResponseAndAssertSuccessStatusCode(validateHearingSubmitted, "validate-hearing-successfully1.json", contestedDir)
            .jsonPath().getList("warnings"), is(empty()));
    }

    @Test
    public void verifyShouldSuccessfullyValidateAboutToSubmit() {
        assertThat(getResponseAndAssertSuccessStatusCode(validateHearing, "validate-hearing-successfully1.json", contestedDir)
            .jsonPath().getList("warnings"), is(empty()));
    }

    private Response getResponse(String url, String jsonFileName, String journeyType) {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).andReturn();
    }

    private Response getResponseAndAssertSuccessStatusCode(String url, String jsonFileName, String journeyType) {
        Response response = getResponse(url,jsonFileName, journeyType);
        assertThat(response.getStatusCode(), is(HttpStatus.SC_OK));
        return response;
    }
}
