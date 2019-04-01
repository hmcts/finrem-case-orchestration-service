package uk.gov.hmcts.reform.finrem.functional.caseorchestration;


import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.json.Json;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)

public class AmendCaseDetailsTest extends IntegrationTestBase {

    @Value("${cos.amend.case.details}")
    private String amendCaseDetailsUrl;
    private JsonPath jsonPathEvaluator;

    @Test
    public void verifyamendDivorceDetailsD81Individual() {
        validatePostSuccess("amend-divorce-details-d81-individual.json");
        jsonPathEvaluator = amendCaseDetails("amend-divorce-details-d81-individual.json");

        if (jsonPathEvaluator.get("d81Joint") != null) {
            Assert.fail("The d81Joint is still showing in the result even after selecting individual.");
        }
    }


    @Test
    public void verifyamendDivorceDetailsD81Joint() {
        validatePostSuccess("amend-divorce-details-d81-joint.json");
        jsonPathEvaluator = amendCaseDetails("amend-divorce-details-d81-joint.json");

        if (jsonPathEvaluator.get("d81Applicant") != null || jsonPathEvaluator.get("d81Respondent") != null ) {
            Assert.fail("The d81Applicant or d81Respondent is still showing in the result even after selecting d81Joint.");
        }
    }

    @Test
    public void verifyamendDivorceDetailsDecreeAbsolute() {
        validatePostSuccess("amend-divorce-details-decree-absolute.json");
        jsonPathEvaluator = amendCaseDetails("amend-divorce-details-decree-absolute.json");

        if (jsonPathEvaluator.get("divorceDecreeNisiDate") != null ) {
            Assert.fail("The divorceDecreeNisiDate is still showing in the result even after selecting decree Absolute.");
        }
    }

    @Test
    public void verifyamendDivorceDetailsDecreeNisi() {
        validatePostSuccess("amend-divorce-details-decree-nisi.json");
        jsonPathEvaluator = amendCaseDetails("amend-divorce-details-decree-nisi.json");

        if (jsonPathEvaluator.get("divorceDecreeAbsoluteDate") != null ) {
            Assert.fail("The divorceDecreeAbsoluteDate is still showing in the result even after selecting decree Absolute.");
        }
    }


    @Test
    public void verifyamendPeriodicPaymentOrder() {
        validatePostSuccess("amend-periodic-payment-order.json");
        jsonPathEvaluator = amendCaseDetails("amend-periodic-payment-order.json");

        if (jsonPathEvaluator.get("natureOfApplication6") != null || jsonPathEvaluator.get("natureOfApplication7") != null) {
            Assert.fail("The periodic payment details with written agreement for children is still showing in the result.");
        }
    }

    @Test
    public void verifyamendPeriodicPaymentOrderwithoutagreement() {
        validatePostSuccess("amend-periodic-payment-order-without-agreement.json");
        jsonPathEvaluator = amendCaseDetails("amend-periodic-payment-order-without-agreement.json");

        if (jsonPathEvaluator.get("natureOfApplication6") == null || jsonPathEvaluator.get("natureOfApplication7") == null) {
            Assert.fail("The periodic payment details with written agreement for children is not showing in the result.");
        }
    }

    @Test
    public void verifyamendPropertyAdjustmentDetails() {
        validatePostSuccess("amend-property-adjustment-details.json");
        jsonPathEvaluator = amendCaseDetails("amend-property-adjustment-details.json");

        if (jsonPathEvaluator.get("natureOfApplication3a") == null || jsonPathEvaluator.get("natureOfApplication3b") == null) {
            Assert.fail("The property adjustment details with written agreement is not showing in the result.");
        }
    }


    @Test
    public void verifyamendRemovePeriodicPaymentOrder() {
        validatePostSuccess("amend-remove-periodic-payment-order.json");
        jsonPathEvaluator = amendCaseDetails("amend-remove-periodic-payment-order.json");

        if (jsonPathEvaluator.get("natureOfApplication5") != null || jsonPathEvaluator.get("natureOfApplication6") != null
        || jsonPathEvaluator.get("natureOfApplication7") != null || jsonPathEvaluator.get("orderForChildrenQuestion1") != null) {
            Assert.fail("The periodic payment details with written agreement for children is still showing in the result.");
        }
    }


    @Test
    public void verifyamendRemovePropertyAdjustmentDetails() {
        validatePostSuccess("remove-property-adjustment-details.json");
        jsonPathEvaluator = amendCaseDetails("remove-property-adjustment-details.json");

        if (jsonPathEvaluator.get("natureOfApplication5") != null || jsonPathEvaluator.get("natureOfApplication6") != null
                || jsonPathEvaluator.get("natureOfApplication7") != null || jsonPathEvaluator.get("orderForChildrenQuestion1") != null) {
            Assert.fail("The property adjustment details with written agreement is still showing in the result.");
        }
    }


    @Test
    public void verifyamendRemoveRespondantSolicitorDetails() {
        validatePostSuccess("remove-respondant-solicitor-details.json");
        jsonPathEvaluator = amendCaseDetails("remove-respondant-solicitor-details.json");

        if (jsonPathEvaluator.get("rSolicitorFirm") != null || jsonPathEvaluator.get("rSolicitorName") != null
                || jsonPathEvaluator.get("rSolicitorReference") != null || jsonPathEvaluator.get("rSolicitorAddress") != null
        || jsonPathEvaluator.get("rSolicitorDXnumber") != null || jsonPathEvaluator.get("rSolicitorEmail") != null
                || jsonPathEvaluator.get("rSolicitorPhone") != null ) {
            Assert.fail("The respondent Solicitor Details are still showing in the result.");
        }
    }

    private void validatePostSuccess(String jsonFileName) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(amendCaseDetailsUrl)
                .then()
                .assertThat().statusCode(200);
    }

    private JsonPath amendCaseDetails(String jsonFileName) {

        Response response = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(amendCaseDetailsUrl).andReturn();

        return response.jsonPath().setRoot("data");
    }


}
