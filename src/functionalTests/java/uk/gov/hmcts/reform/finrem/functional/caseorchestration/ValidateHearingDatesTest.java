package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
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

    @Value("${cos.validate.hearing}")
    private String validateHearing;

    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";

    @Test
    public void verifyShouldReturnBadRequestWhenCaseDataIsMissingInRequest() {
        Response response = utils.getResponse(validateHearing, "empty-casedata1.json",consentedDir);

        assertThat(response.getStatusCode(), is(400));
        assertThat(response.body().asString(), startsWith("Some server side exception occurred. Please check logs for details"));
    }

    @Test
    public void verifyShouldThrowErrorWhenIssueDateAndHearingDateAreEmpty() {
        assertThat(getResponseAndAssertSuccessStatus(validateHearing, "pba-validate1.json",consentedDir).jsonPath().get("errors[0]"),
            is("Issue Date, fast track decision or hearingDate is empty"));
    }

    @Test
    public void verifyShouldThrowWarningsWhenNotFastTrackDecision() {
        assertThat(getResponseAndAssertSuccessStatus(validateHearing, "validate-hearing-without-fastTrackDecision1.json",
            contestedDir).jsonPath().get("warnings[0]"), is("Date of the hearing must be between 12 and 16 weeks."));
    }

    @Test
    public void verifyshouldThrowWarningsWhenFastTrackDecision() {
        assertThat(getResponseAndAssertSuccessStatus(validateHearing, "validate-hearing-with-fastTrackDecision1.json",
            contestedDir).jsonPath().get("warnings[0]"), is("Date of the Fast Track hearing must be between 6 and 10 weeks."));
    }

    @Test
    public void verifyShouldSuccessfullyValidate() {
        assertThat(getResponseAndAssertSuccessStatus(validateHearing, "validate-hearing-successfully1.json",
            contestedDir).jsonPath().get("warnings"), is(empty()));
    }

    private Response getResponseAndAssertSuccessStatus(String url, String jsonFileName, String journeyType) {
        Response response = utils.getResponse(url, jsonFileName, journeyType);
        assertThat(response.getStatusCode(), is(200));
        return response;
    }
}
