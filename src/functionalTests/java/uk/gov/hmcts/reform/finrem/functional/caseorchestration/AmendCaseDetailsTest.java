package uk.gov.hmcts.reform.finrem.functional.caseorchestration;


import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)

public class AmendCaseDetailsTest extends IntegrationTestBase {

    @Value("${cos.amend.case.details}")
    private String amendCaseDetailsUrl;

    @Test
    public void verifyamendDivorceDetailsD81Individual() {
        validatePostSuccess("ccd-request-with-solicitor-assignedToJudge.json");
    }

    private void validatePostSuccess(String jsonFileName) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post("http://localhost:9000/case-orchestration/update-case")
                .then()
                .assertThat().statusCode(200);
    }
}
