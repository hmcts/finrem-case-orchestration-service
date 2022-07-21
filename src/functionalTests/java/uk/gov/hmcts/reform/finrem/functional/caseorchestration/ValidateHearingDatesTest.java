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
    private String hearing;

    private static final String CONTESTED_DIR = "/json/contested/";
    private static final String CONSENTED_DIR = "/json/consented/";

    @Test
    public void verifyShouldReturnBadRequestWhenCaseDataIsMissingInRequest() {
        Response response = utils.getResponse(hearing, "empty-casedata1.json", CONSENTED_DIR);

        assertThat(response.getStatusCode(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(response.body().asString(), startsWith("Some server side exception occurred. Please check logs for details"));
    }

    @Test
    public void verifyShouldThrowErrorWhenIssueDateAndHearingDateAreEmpty() {
        assertThat(getResponseAndAssertSuccessStatusCode(hearing, "pba-validate1.json", CONTESTED_DIR).jsonPath().get("errors[0]"),
            is("Issue Date, fast track decision or hearingDate is empty"));
    }

    @Test
    public void verifyShouldThrowWarningsWhenNotFastTrackDecision() {
        assertThat(getResponseAndAssertSuccessStatusCode(hearing, "validate-hearing-without-fastTrackDecision1.json",
            CONTESTED_DIR).jsonPath().get("warnings[0]"), is("Date of the hearing must be between 12 and 16 weeks."));
    }

    @Test
    public void verifyshouldThrowWarningsWhenFastTrackDecision() {
        assertThat(getResponseAndAssertSuccessStatusCode(hearing, "validate-hearing-with-fastTrackDecision1.json",
            CONTESTED_DIR).jsonPath().get("warnings[0]"), is("Date of the Fast Track hearing must be between 6 and 10 weeks."));
    }

    @Test
    public void verifyShouldSuccessfullyValidate() {
        assertThat(getResponseAndAssertSuccessStatusCode(hearing, "validate-hearing-successfully1.json", CONTESTED_DIR)
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
