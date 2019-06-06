package uk.gov.hmcts.reform.finrem.functional.caseorchestration;


import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertEquals;


@RunWith(SerenityRunner.class)

public class AmendCaseDetailsTest extends IntegrationTestBase {

    @Value("${cos.amend.case.details}")
    private String amendCaseDetailsUrl;

    @Value("${cos.amend.contested.case.details}")
    private String amendContestedCaseDetailsUrl;

    private JsonPath jsonPathEvaluator;
    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";
    private String dataPath = "data";

    @Test
    public void verifyamendDivorceDetailsD81Individual() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-divorce-details-d81-individual1.json");

        if (jsonPathEvaluator.get("d81Joint") != null) {
            Assert.fail("The d81Joint is still showing in the result even after selecting individual.");
        }
    }

    @Test
    public void verifyamendDivorceDetailsD81Joint() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,"amend-divorce-details-d81-joint.json");

        if (jsonPathEvaluator.get("d81Applicant") != null
                || jsonPathEvaluator.get("d81Respondent") != null ) {
            Assert.fail("The d81Applicant or d81Respondent is still showing in the result even after "
                    + "selecting d81Joint.");
        }
    }

    @Test
    public void verifyamendDivorceDetailsDecreeAbsolute() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-divorce-details-decree-absolute1.json");

        if (jsonPathEvaluator.get("divorceDecreeNisiDate") != null ) {
            Assert.fail("The divorceDecreeNisiDate is still showing in the result even after"
                    + " selecting decree Absolute.");
        }
    }

    @Test
    public void verifyamendDivorceDetailsDecreeNisi() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-divorce-details-decree-nisi1.json");

        if (jsonPathEvaluator.get("divorceDecreeAbsoluteDate") != null ) {
            Assert.fail("The divorceDecreeAbsoluteDate is still showing in the result even after"
                    + " selecting decree Absolute.");
        }
    }


    @Test
    public void verifyamendPeriodicPaymentOrder() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,"amend-periodic-payment-order1.json");

        if (jsonPathEvaluator.get("natureOfApplication6") != null
                || jsonPathEvaluator.get("natureOfApplication7") != null) {
            Assert.fail("The periodic payment details with written agreement for children is "
                    + "still showing in the result.");
        }
    }

    @Test
    public void verifyamendPeriodicPaymentOrderwithoutagreement() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-periodic-payment-order-without-agreement1.json");

        if (jsonPathEvaluator.get("natureOfApplication6") == null
                || jsonPathEvaluator.get("natureOfApplication7") == null) {
            Assert.fail("The periodic payment details with written agreement for children is not "
                    + "showing in the result.");
        }
    }

    @Test
    public void verifyamendPropertyAdjustmentDetails() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-property-adjustment-details1.json");

        if (jsonPathEvaluator.get("natureOfApplication3a") == null
                || jsonPathEvaluator.get("natureOfApplication3b") == null) {
            Assert.fail("The property adjustment details with written agreement is not"
                    + " showing in the result.");
        }
    }


    @Test
    public void verifyamendRemovePeriodicPaymentOrder() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "amend-remove-periodic-payment-order1.json");

        if (jsonPathEvaluator.get("natureOfApplication5") != null
                || jsonPathEvaluator.get("natureOfApplication6") != null
                || jsonPathEvaluator.get("natureOfApplication7") != null
                || jsonPathEvaluator.get("orderForChildrenQuestion1") != null) {
            Assert.fail("The periodic payment details with written agreement for children"
                    + " is still showing in the result.");
        }
    }


    @Test
    public void verifyamendRemovePropertyAdjustmentDetails() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "remove-property-adjustment-details1.json");

        if (jsonPathEvaluator.get("natureOfApplication5") != null
                || jsonPathEvaluator.get("natureOfApplication6") != null
                || jsonPathEvaluator.get("natureOfApplication7") != null
                || jsonPathEvaluator.get("orderForChildrenQuestion1") != null) {
            Assert.fail("The property adjustment details with written agreement is still"
                    + " showing in the result.");
        }
    }


    @Test
    public void verifyamendRemoveRespondantSolicitorDetails() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,consentedDir,
                "remove-respondant-solicitor-details1.json");

        if (jsonPathEvaluator.get("rSolicitorFirm") != null
                || jsonPathEvaluator.get("rSolicitorName") != null
                || jsonPathEvaluator.get("rSolicitorReference") != null
                || jsonPathEvaluator.get("rSolicitorAddress") != null
                || jsonPathEvaluator.get("rSolicitorDXnumber") != null
                || jsonPathEvaluator.get("rSolicitorEmail") != null
                || jsonPathEvaluator.get("rSolicitorPhone") != null ) {
            Assert.fail("The respondent Solicitor Details are still showing "
                    + "in the result.");
        }
    }



    @Test
    public void verifyDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,contestedDir,
                "amend-divorce-details-decree-nisi1.json");

        if (jsonPathEvaluator.get("divorceUploadEvidence2") != null
                || jsonPathEvaluator.get("divorceDecreeAbsoluteDate") != null ) {

            Assert.fail("The decree nissi file is still showing "
                    + "in the result.");
        }

    }


    @Test
    public void verifyDeleteDecreeAbsoluteWhenSolicitorChooseToDecreeNisiForContested() {

        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl,contestedDir,
                "amend-divorce-details-decree-absolute1.json");

        if (jsonPathEvaluator.get("divorceUploadEvidence1") != null
                || jsonPathEvaluator.get("divorceDecreeNisiDate") != null ) {

            Assert.fail("The decree Absolute file is still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-property-adjustment-order-details1.json");

        if (jsonPathEvaluator.get("propertyAddress") != null
                || jsonPathEvaluator.get("mortgageDetail") != null
                || jsonPathEvaluator.get("propertyAdjutmentOrderDetail") != null) {

            Assert.fail("The property details are still showing "
                    + "in the result.");
        }
    }


    @Test
    public void verifyRemoveAdditionalPropertyDetailsForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-additional-property-details1.json");

        if ( jsonPathEvaluator.get("propertyAdjutmentOrderDetail") != null) {

            Assert.fail("The additional property details are still showing "
                    + "in the result.");
        }
    }

    @Test
    public void verifyRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-periodic-payment-order-details1.json");

        if (jsonPathEvaluator.get("paymentForChildrenDecision") != null
                || jsonPathEvaluator.get("benefitForChildrenDecision") != null
                || jsonPathEvaluator.get("benefitPaymentChecklist") != null) {

            Assert.fail("The periodic payment details are still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyUpdatePeriodicPaymentDetailsWhenPaymentForChildrenIsUncheckedForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "update-periodic-payment-details-for-no-payment-for-children1.json");

        if (jsonPathEvaluator.get("benefitForChildrenDecision") != null
                || jsonPathEvaluator.get("benefitPaymentChecklist") != null
        ) {

            Assert.fail("The periodic payment details are still showing even after "
                    + "payment for children is unchecked for contested"
                    + "in the result.");
        }

    }

    @Test
    public void verifyUpdatePeriodicPaymentDetailsWhenBenefitsForChildrenForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "update-periodic-payment-details-with-benefits-for-children1.json");

        if ( jsonPathEvaluator.get("benefitPaymentChecklist") != null
        ) {

            Assert.fail("The benefitPaymentCheckList is still showing"
                    + "in the result.");
        }
    }

    @Test
    public void verifyRemoveSolicitorDetailsWhenRespondentIsNotRepresentedBySolicitorForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-respondent-solicitor-details1.json");

        if (jsonPathEvaluator.get("rSolicitorName") != null
                || jsonPathEvaluator.get("rSolicitorFirm") != null
                || jsonPathEvaluator.get("rSolicitorReference") != null
                || jsonPathEvaluator.get("rSolicitorEmail") != null
                || jsonPathEvaluator.get("rSolicitorAddress") != null
                || jsonPathEvaluator.get("rSolicitorPhone") != null
                || jsonPathEvaluator.get("rSolicitorDXnumber") != null

        ) {

            Assert.fail("The respondent solicitor details are still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyRemoveRespondentAddressWhenRespondentIsRepresentedBySolicitorForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-respondent-address-details1.json");

        if ( jsonPathEvaluator.get("respondentAddress") != null
                || jsonPathEvaluator.get("respondentPhone") != null
                || jsonPathEvaluator.get("respondentEmail") != null
        ) {

            Assert.fail("The respondent solicitor address details are still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyUpdateFastTrackDetailsForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-respondent-address-details1.json");

        if ( jsonPathEvaluator.get("fastTrackDecisionReason") != null

        ) {

            Assert.fail("The fastTrackDecisionReason  details are still showing "
                    + "in the result.");
        }
    }

    @Test
    public void verifyShouldNotUpdateFastTrackDetailsForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-respondent-solicitor-details1.json");

        if ( jsonPathEvaluator.get("fastTrackDecisionReason") == null

        ) {

            Assert.fail("The fastTrackDecisionReason  details are not showing "
                    + "in the result.");
        }
    }

    @Test
    public void verifyshouldRemoveComplexityDetailsForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-complexity-details1.json");

        if ( jsonPathEvaluator.get("estimatedAssetsChecklist") != null
                || jsonPathEvaluator.get("netValueOfHome") != null
                || jsonPathEvaluator.get("potentialAllegationChecklist") != null
                || jsonPathEvaluator.get("otherReasonForComplexity") != null
                || jsonPathEvaluator.get("otherReasonForComplexityText") != null
                || jsonPathEvaluator.get("detailPotentialAllegation") != null

        ) {

            Assert.fail("The complexity details for contested are still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyshouldRemoveAdditionalReasonForComplexityForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-other-reason-for-complexity1.json");

        if ( jsonPathEvaluator.get("otherReasonForComplexityText") != null

        ) {
            Assert.fail("The other complexity details are not showing "
                    + "in the result.");
        }
    }

    @Test
    public void verifyshouldRemoveReasonForLocalCourtForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "is-applicant-home-court1.json");

        if ( jsonPathEvaluator.get("reasonForLocalCourt") != null

        ) {
            Assert.fail("The reason for local court details are not showing "
                    + "in the result.");
        }
    }


    @Test
    public void verifyshouldRemoveMiamExceptionsWhenApplicantAttendedMiamForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-exceptions-when-applicant-attended-miam1.json");

        if ( jsonPathEvaluator.get("claimingExemptionMIAM") != null
                || jsonPathEvaluator.get("familyMediatorMIAM") != null
                || jsonPathEvaluator.get("MIAMExemptionsChecklist") != null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") != null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") != null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") != null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") != null

        ) {

            Assert.fail("The Miam exception details when applicant attended Miam for contested are still showing "
                    + "in the result.");
        }

    }

    @Test
    public void verifyshouldUpdateMiamExceptionsWhenApplicantNotClaimingExceptionsForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "update-miam-exceptions-when-applicant-not-claiming-exemption1.json");

        if ( jsonPathEvaluator.get("applicantAttendedMIAM") == null
                || jsonPathEvaluator.get("claimingExemptionMIAM") == null
                || jsonPathEvaluator.get("familyMediatorMIAM") != null
                || jsonPathEvaluator.get("MIAMExemptionsChecklist") != null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") != null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") != null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") != null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") != null

        ) {

            Assert.fail("The Miam exception details when applicant attended"
                    +
                    " Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }


    @Test
    public void verifyshouldUpdateMiamExceptionsWhenApplicantHasFamilyMediatorForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "update-miam-exceptions-when-applicant-attended-family-mediator1.json");

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") != null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") != null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") != null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") != null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") != null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }

    @Test
    public void verifyShouldRemoveDomesticViolenceCheckListForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-domestic-violence-checklist1.json");

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") == null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") != null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") == null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") == null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") == null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }

    @Test
    public void verifyShouldRemoveUrgencyCheckListForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-urgency-checklist1.json");

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") == null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") == null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") != null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") == null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") == null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }

    @Test
    public void verifyShouldRemovePreviousMiamAttendanceCheckListForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-previousMiamAttendance-checklist1.json");
        System.out.println("response content : " + jsonPathEvaluator.prettyPrint());

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") == null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") == null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") == null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") != null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") == null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }

    @Test
    public void verifyShouldRemoveOtherGroundsMiamCheckListForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "remove-other-checklist1.json");

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") == null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") == null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") == null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") == null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") != null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are not correctly updated  "
                    + "in the result.");
        }
    }


    @Test
    public void verifyShouldNotRemoveMiamCheckListForContested() {

        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl,contestedDir,
                "do-not-remove-checklists1.json");

        if (     jsonPathEvaluator.get("MIAMExemptionsChecklist") == null
                || jsonPathEvaluator.get("MIAMDomesticViolenceChecklist") == null
                || jsonPathEvaluator.get("MIAMUrgencyReasonChecklist") == null
                || jsonPathEvaluator.get("MIAMPreviousAttendanceChecklist") == null
                || jsonPathEvaluator.get("MIAMOtherGroundsChecklist") == null

        ) {

            Assert.fail("The Miam exception details when applicant attended "
                    +
                    "Miam for contested are removed  "
                    + "in the result.");
        }
    }

    private JsonPath amendCaseDetails(String url, String journeyType, String jsonFileName) {

        Response response = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).andReturn();

        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200);
        return response.jsonPath().setRoot("data");
    }


}
