package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAMILY_MEDIATOR_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_ADDITIONAL_INFO_OTHER_GROUNDS_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_ABUSE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_VIOLENCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_EXEMPTIONS_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST_V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;


@RunWith(SerenityRunner.class)
public class AmendCaseDetailsTest extends IntegrationTestBase {

    @Value("${cos.amend.case.details}")
    private String amendCaseDetailsUrl;

    @Value("${cos.amend.contested.case.details}")
    private String amendContestedCaseDetailsUrl;

    private JsonPath jsonPathEvaluator;
    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";

    @Test
    public void verifyAmendDivorceDetailsD81Individual() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-divorce-details-d81-individual1.json");
        checkIsNull("d81Joint", ", even after selecting individual,", jsonPathEvaluator);
    }

    @Test
    public void verifyAmendDivorceDetailsD81Joint() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir, "amend-divorce-details-d81-joint.json");
        String message = ", even after selecting d81Joint,";
        checkIsNull("d81Applicant", message, jsonPathEvaluator);
        checkIsNull("d81Respondent", message, jsonPathEvaluator);
    }

    @Test
    public void verifyAmendDivorceDetailsDecreeAbsolute() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-divorce-details-decree-absolute1.json");
        checkIsNull("divorceDecreeNisiDate", ", even after selecting decree absolute,", jsonPathEvaluator);
    }

    @Test
    public void verifyAmendDivorceDetailsDecreeNisi() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-divorce-details-decree-nisi1.json");

        checkIsNull("divorceDecreeAbsoluteDate", ", even after selecting decree Absolute,", jsonPathEvaluator);
    }

    @Test
    public void verifyAmendPeriodicPaymentOrder() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir, "amend-periodic-payment-order1.json");
        String message = " associated with periodic payment details and written agreement for children";
        checkIsNull("natureOfApplication6", message, jsonPathEvaluator);
        checkIsNull("natureOfApplication7", message, jsonPathEvaluator);
    }

    @Test
    public void verifyAmendPeriodicPaymentOrderwithoutagreement() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-periodic-payment-order-without-agreement1.json");
        String message = " associated with periodic payment details and written agreement for children";
        checkNotNull("natureOfApplication6", message, jsonPathEvaluator);
        checkNotNull("natureOfApplication7", message, jsonPathEvaluator);
    }

    @Test
    public void verifyAmendPropertyAdjustmentDetails() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-property-adjustment-details1.json");
        String message = " associated with property adjustment details and written agreement";
        checkNotNull("natureOfApplication3a", message, jsonPathEvaluator);
        checkNotNull("natureOfApplication3b", message, jsonPathEvaluator);
    }

    @Test
    public void verifyAmendRemovePeriodicPaymentOrder() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-remove-periodic-payment-order1.json");
        String message = " associated with periodic payment details and written agreement for children";
        String[] fields = {
            "natureOfApplication5",
            "natureOfApplication6",
            "natureOfApplication7",
            "orderForChildrenQuestion1"
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyAmendRemovePropertyAdjustmentDetails() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "remove-property-adjustment-details1.json");
        String message = " associated with property adjustment details and written agreement";
        String[] fields = {
            "natureOfApplication5",
            "natureOfApplication6",
            "natureOfApplication7",
            "orderForChildrenQuestion1"
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyAmendRemoveRespondentSolicitorDetails() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "remove-respondent-solicitor-details1.json");
        String message = " associated with the respondent solicitor details";
        String[] fields = {
            RESP_SOLICITOR_FIRM,
            RESP_SOLICITOR_NAME,
            RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_ADDRESS,
            RESP_SOLICITOR_DX_NUMBER,
            RESP_SOLICITOR_EMAIL,
            RESP_SOLICITOR_PHONE
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, contestedDir,
            "amend-divorce-details-decree-nisi-handler.json");
        checkIsNull("divorceUploadEvidence2", null, jsonPathEvaluator);
        checkIsNull("divorceDecreeAbsoluteDate", null, jsonPathEvaluator);
    }

    @Test
    public void verifyDeleteDecreeAbsoluteWhenSolicitorChooseToDecreeNisiForContested() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, contestedDir,
            "amend-divorce-details-decree-absolute-handler.json");
        checkIsNull("divorceUploadEvidence1", null, jsonPathEvaluator);
        checkIsNull("divorceDecreeNisiDate", null, jsonPathEvaluator);
    }

    @Test
    public void verifyRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-property-adjustment-order-details1.json");
        String message = " associated with property details";
        String[] fields = {
            "propertyAddress",
            "mortgageDetail",
            "propertyAdjutmentOrderDetail"
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyRemoveAdditionalPropertyDetailsForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-additional-property-details1.json");
        checkIsNull("propertyAdjutmentOrderDetail", " associated with additional property details", jsonPathEvaluator);
    }

    @Test
    public void verifyRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-periodic-payment-order-details1.json");
        String message = " associated with periodic payment details";
        String[] fields = {
            "paymentForChildrenDecision",
            "benefitForChildrenDecision",
            "benefitPaymentChecklist"
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyUpdatePeriodicPaymentDetailsWhenPaymentForChildrenIsUncheckedForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "update-periodic-payment-details-for-no-payment-for-children1.json");
        String message = " associated with the periodic payment details, even after payment for children is unchecked,";
        checkIsNull("benefitForChildrenDecision", message, jsonPathEvaluator);
        checkIsNull("benefitPaymentChecklist", message, jsonPathEvaluator);
    }

    @Test
    public void verifyUpdatePeriodicPaymentDetailsWhenBenefitsForChildrenForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "update-periodic-payment-details-with-benefits-for-children1.json");
        checkIsNull("benefitPaymentChecklist", null, jsonPathEvaluator);
    }

    @Test
    public void verifyRemoveSolicitorDetailsWhenRespondentIsNotRepresentedBySolicitorForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-respondent-solicitor-details1.json");
        String message = " associated with the respondent solicitor details";
        String[] fields = {
            RESP_SOLICITOR_NAME,
            RESP_SOLICITOR_FIRM,
            RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_EMAIL,
            RESP_SOLICITOR_ADDRESS,
            RESP_SOLICITOR_PHONE,
            RESP_SOLICITOR_DX_NUMBER
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyRemoveRespondentAddressWhenRespondentIsRepresentedBySolicitorForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-respondent-address-details1.json");
        String message = " associated with the respondent solicitor address details";
        String[] fields = {
            RESPONDENT_ADDRESS,
            RESPONDENT_PHONE,
            RESPONDENT_EMAIL
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyUpdateFastTrackDetailsForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-respondent-address-details1.json");
        checkIsNull("fastTrackDecisionReason", null, jsonPathEvaluator);
    }

    @Test
    public void verifyShouldNotUpdateFastTrackDetailsForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-respondent-solicitor-details1.json");
        checkNotNull("fastTrackDecisionReason", null, jsonPathEvaluator);
    }

    @Test
    public void verifyShouldUpdateComplexityDetailsForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-complexity-details1.json");
        String message = ", associated with the complexity details,";
        String[] fields = {
            "estimatedAssetsChecklist",
            "netValueOfHome",
            "potentialAllegationChecklist",
            "otherReasonForComplexity",
            "otherReasonForComplexityText",
            "detailPotentialAllegation"
        };
        for (String field : fields) {
            checkNotNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyShouldRemoveAdditionalReasonForComplexityForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-other-reason-for-complexity1.json");
        checkIsNull("otherReasonForComplexityText", null, jsonPathEvaluator);
    }

    @Test
    public void verifyShouldRemoveReasonForLocalCourtForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "is-applicant-home-court1.json");
        checkIsNull("reasonForLocalCourt", null, jsonPathEvaluator);
    }

    @Test
    public void verifyShouldRemoveMiamExemptionsAndCertificationWhenApplicantAttendedMiamForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "remove-exceptions-when-applicant-attended-miam1.json");
        String message = " associated with the Miam exemption or certification details";
        String[] fields = {
            CLAIMING_EXEMPTION_MIAM,
            FAMILY_MEDIATOR_MIAM,
            MIAM_EXEMPTIONS_CHECKLIST,
            MIAM_DOMESTIC_VIOLENCE_CHECKLIST,
            MIAM_URGENCY_CHECKLIST,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
            MIAM_OTHER_GROUNDS_CHECKLIST,
            MIAM_DOMESTIC_ABUSE_TEXTBOX,
            MIAM_URGENCY_TEXTBOX,
            MIAM_PREVIOUS_ATTENDANCE_TEXTBOX,
            MIAM_OTHER_GROUNDS_TEXTBOX,
            MIAM_ADDITIONAL_INFO_OTHER_GROUNDS_TEXTBOX,
            "soleTraderName1",
            "familyMediatorServiceName1",
            "mediatorRegistrationNumber1"
        };
        for (String field : fields) {
            checkIsNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyShouldNotRemoveMiamCheckListsForContested() {
        jsonPathEvaluator = amendCaseDetails(amendContestedCaseDetailsUrl, contestedDir,
            "do-not-remove-checklists1.json");
        String message = " associated with Miam exemptions when the applicant did not attend a Miam and is claiming exemption";
        checkExemptionFieldsNotNull(message);
        checkIsNull("familyMediatorMIAM", " which is a legacy field", jsonPathEvaluator);
    }

    private void checkExemptionFieldsNotNull(String message) {
        String[] fields = getMiamExemptionFields();
        for (String field : fields) {
            checkNotNull(field, message, jsonPathEvaluator);
        }
    }

    @Test
    public void verifyShouldUpdateCaseDataWithLatestConsentOrder() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-consent-order-by-solicitor.json");
        checkNotNull(LATEST_CONSENT_ORDER, null,
            jsonPathEvaluator);
    }

    @Test
    public void verifyShouldSetLatestDraftConsentOrderWhenACaseIsCreated() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "draft-consent-order.json");
        checkLatestConsentOrderDocument("http://file1.binary", "file1", "http://file1");
    }

    @Test
    public void verifyShouldUpdateLatestDraftConsentOrderWhenACaseIsAmended() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-consent-order-by-solicitor.json");
        checkLatestConsentOrderDocument("http://file2.binary", "file2", "http://file2");
    }

    @Test
    public void shouldReturnLatestAmendedConsentOrderWhenACaseIsAmendedByCaseWorker() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "amend-consent-order-by-caseworker.json");
        checkLatestConsentOrderDocument("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary",
            "Notification for ABC - Contested.docx", "http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838");
    }

    @Test
    public void shouldReturnLatestAmendedConsentOrderWhenACaseIsRespondedBySolicitor() {
        jsonPathEvaluator = amendCaseDetails(amendCaseDetailsUrl, consentedDir,
            "respond-to-order-solicitor.json");
        checkLatestConsentOrderDocument("http://doc1/binary", "doc1", "http://doc1");
    }

    private JsonPath amendCaseDetails(String url, String journeyType, String jsonFileName) {
        Response response = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).andReturn();

        assertEquals(200, response.getStatusCode());
        return response.jsonPath().setRoot("data");
    }

    private void checkLatestConsentOrderDocument(String url, String file1, String url1) {
        assertThat(jsonPathEvaluator.get("latestConsentOrder.document_binary_url"), is(url));
        assertThat(jsonPathEvaluator.get("latestConsentOrder.document_filename"), is(file1));
        assertThat(jsonPathEvaluator.get("latestConsentOrder.document_url"), is(url1));
    }

    private void checkNotNull(String field, String message, JsonPath jsonPathEvaluator) {
        assertNotNull("The field " + field + message + " is not showing in the result.", jsonPathEvaluator.get(field));
    }

    private void checkIsNull(String field, String message, JsonPath jsonPathEvaluator) {
        assertNull("The field " + field + message + " is still showing in the result.", jsonPathEvaluator.get(field));
    }

    private String[] getMiamExemptionFields() {
        return new String[]{
            CLAIMING_EXEMPTION_MIAM,
            MIAM_EXEMPTIONS_CHECKLIST,
            MIAM_DOMESTIC_VIOLENCE_CHECKLIST,
            MIAM_URGENCY_CHECKLIST,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2,
            MIAM_OTHER_GROUNDS_CHECKLIST_V2,
            MIAM_DOMESTIC_ABUSE_TEXTBOX,
            MIAM_URGENCY_TEXTBOX,
            MIAM_PREVIOUS_ATTENDANCE_TEXTBOX,
            MIAM_OTHER_GROUNDS_TEXTBOX,
            MIAM_ADDITIONAL_INFO_OTHER_GROUNDS_TEXTBOX
        };
    }

}
