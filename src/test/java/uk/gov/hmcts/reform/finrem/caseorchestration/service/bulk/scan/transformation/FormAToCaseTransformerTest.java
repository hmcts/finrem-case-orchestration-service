package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FormAToCaseTransformer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BULK_SCAN_CASE_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;

public class FormAToCaseTransformerTest {

    public static final String PAPER_APPLICATION = "paperApplication";

    private final FormAToCaseTransformer formAToCaseTransformer = new FormAToCaseTransformer();

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
                new OcrDataField(OcrFieldName.DIVORCE_CASE_NUMBER, "1234567890"),
                new OcrDataField(OcrFieldName.HWF_NUMBER, "123456"),
                new OcrDataField(OcrFieldName.APPLICANT_FULL_NAME, "Peter Griffin"),
                new OcrDataField(OcrFieldName.RESPONDENT_FULL_NAME, "Louis Griffin"),
                new OcrDataField(OcrFieldName.PROVISION_MADE_FOR, "in connection with matrimonial or civil partnership proceedings"),
                new OcrDataField(OcrFieldName.NATURE_OF_APPLICATION, "Periodical Payment Order, Pension Attachment Order"),
                new OcrDataField(OcrFieldName.APPLICANT_INTENDS_TO, "ApplyToCourtFor"),
                new OcrDataField(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "a lump sum order, a pension sharing order"),
                new OcrDataField(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "Yes"),
                new OcrDataField(OcrFieldName.DIVORCE_STAGE_REACHED, "Decree Nisi"),
                new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED, "I am not represented by a solicitor in these proceedings"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_NAME, "Saul Call"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_FIRM, "Better Divorce Ltd"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_PHONE, "0712456543"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER, "DX123"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_REFERENCE, "SOL-RED"),
                new OcrDataField(OcrFieldName.APPLICANT_PBA_NUMBER, "PBA123456"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_EMAIL, "test@example.com"),
                new OcrDataField(OcrFieldName.APPLICANT_PHONE, "0712345654"),
                new OcrDataField(OcrFieldName.APPLICANT_EMAIL, "applicant@divorcity.com"),
                new OcrDataField(OcrFieldName.ADDRESS_OF_PROPERTIES, "The address of other properties"),
                new OcrDataField(OcrFieldName.MORTGAGE_DETAILS, "Various details of our mortgage"),
                new OcrDataField(OcrFieldName.ORDER_FOR_CHILDREN,
                        "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children"),
                new OcrDataField(OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT,
                        "in addition to child support maintenance already paid under a Child Support Agency assessment"),
                new OcrDataField(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "Yes"),
                new OcrDataField(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "Random reason that explains calculation"),
                new OcrDataField(OcrFieldName.AUTHORISATION_NAME, "Saul B. Kol"),
                new OcrDataField(OcrFieldName.AUTHORISATION_FIRM, "Better Divorce Ltd"),
                new OcrDataField(OcrFieldName.AUTHORISATION_SOLICITOR_ADDRESS, "1 Single Lane, Liverpool, LE5 AV2"),
                new OcrDataField(OcrFieldName.AUTHORISATION_SIGNED, "Yes"),
                new OcrDataField(OcrFieldName.AUTHORISATION_SIGNED_BY, "Applicant's solicitor"),
                new OcrDataField(OcrFieldName.AUTHORISATION_DATE, "12/03/2020"),
                new OcrDataField(OcrFieldName.AUTHORISATION_SOLICITOR_POSITION, "I'm the CEO"),
                new OcrDataField(OcrFieldName.NAME_CHILD_1, "Johny Bravo"),
                new OcrDataField(OcrFieldName.GENDER_CHILD_1, "male"),
                new OcrDataField(OcrFieldName.DATE_OF_BIRTH_CHILD_1, "12/03/2000"),
                new OcrDataField(OcrFieldName.RELATIONSHIP_TO_APPLICANT_CHILD_1, "son"),
                new OcrDataField(OcrFieldName.RELATIONSHIP_TO_RESPONDENT_CHILD_1, "SON"),
                new OcrDataField(OcrFieldName.COUNTRY_CHILD_1, "New Zeeland"),
                new OcrDataField(OcrFieldName.NAME_CHILD_2, "Anne Shirley"),
                new OcrDataField(OcrFieldName.GENDER_CHILD_2, "female"),
                new OcrDataField(OcrFieldName.DATE_OF_BIRTH_CHILD_2, "12/03/1895"),
                new OcrDataField(OcrFieldName.RELATIONSHIP_TO_APPLICANT_CHILD_2, "daughter"),
                new OcrDataField(OcrFieldName.RELATIONSHIP_TO_RESPONDENT_CHILD_2, "Daughter"),
                new OcrDataField(OcrFieldName.COUNTRY_CHILD_2, "Canada")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry(CCDConfigConstant.DIVORCE_CASE_NUMBER, "1234567890"),
                hasEntry("HWFNumber", "123456"),
                hasEntry("applicantFMName", "Peter"),
                hasEntry("applicantLName", "Griffin"),
                hasEntry("appRespondentFMname", "Louis"),
                hasEntry("appRespondentLName", "Griffin"),
                hasEntry("provisionMadeFor", "matrimonialOrCivilPartnershipProceedings"),
                hasEntry("applicantIntendsTo", "ApplyToCourtFor"),
                hasEntry("applyingForConsentOrder", "Yes"),
                hasEntry("divorceStageReached", "Decree Nisi"),
                hasEntry("applicantRepresentPaper", "FR_applicant_represented_1"),
                hasEntry(CCDConfigConstant.SOLICITOR_NAME, "Saul Call"),
                hasEntry(CCDConfigConstant.SOLICITOR_FIRM, "Better Divorce Ltd"),
                hasEntry("solicitorPhone", "0712456543"),
                hasEntry("solicitorDXnumber", "DX123"),
                hasEntry("solicitorReference", "SOL-RED"),
                hasEntry(CCDConfigConstant.PBA_NUMBER, "PBA123456"),
                hasEntry(CCDConfigConstant.SOLICITOR_EMAIL, "test@example.com"),
                hasEntry("applicantPhone", "0712345654"),
                hasEntry("applicantEmail", "applicant@divorcity.com"),

                hasEntry("natureOfApplication3a", "The address of other properties"),
                hasEntry("natureOfApplication3b", "Various details of our mortgage"),
                hasEntry("natureOfApplication5b",
                        "FR_nature_of_application_2"),
                hasEntry("orderForChildrenQuestion1", "Yes"),
                hasEntry("ChildSupportAgencyCalculationMade", "Yes"),
                hasEntry("ChildSupportAgencyCalculationReason", "Random reason that explains calculation"),
                hasEntry("authorisationName", "Saul B. Kol"),
                hasEntry("authorisationFirm", "Better Divorce Ltd"),
                hasEntry("authorisationSolicitorAddress", "1 Single Lane, Liverpool, LE5 AV2"),
                hasEntry("authorisationSigned", "Yes"),
                hasEntry("authorisationSignedBy", "Applicant's solicitor"),
                hasEntry("authorisation3", "2020-03-12"),
                hasEntry("authorisation2b", "I'm the CEO")
        ));

        assertChildrenInfo(transformedCaseData);

        assertThat(transformedCaseData.get("natureOfApplication2"), is(asList("Periodical Payment Order", "Pension Attachment Order")));
        assertThat(transformedCaseData.get("dischargePeriodicalPaymentSubstituteFor"), is(asList("Lump Sum Order", "Pension Sharing Order")));
        assertThat(transformedCaseData.get("natureOfApplication6"), is(singletonList("In addition to child support")));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
                new OcrDataField("UnexpectedName", "UnexpectedValue")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
                aMapWithSize(2),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE)
        ));
    }

    @Test
    public void shouldNotReturnExpectedFieldsWithNullValue() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(asList(
                new OcrDataField(APPLICANT_FULL_NAME, null),
                new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, null)
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
                aMapWithSize(2),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE)
        ));
    }

    @Test
    public void shouldTransformAddresses() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(Arrays.asList(
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_LINE_1, "Street"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_TOWN, "London"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_COUNTY, "Great London"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_POSTCODE, "SW989SD"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_COUNTRY, "UK"),

                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_LINE_1, "Road"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_TOWN, "Manchester"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_COUNTY, "There"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_POSTCODE, "SW9 9SD"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_COUNTRY, "Germany"),

                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_LINE_1, "Avenue"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_TOWN, "Bristol"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_COUNTY, "Here"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_POSTCODE, "SW1 9SD"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_COUNTRY, "France"),

                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_LINE_1, "Drive"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_TOWN, "Leeds"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_COUNTY, "Where"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_POSTCODE, "SW9 USB"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_COUNTRY, "Scotland")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
                aMapWithSize(6),    // 5 = BULK_SCAN_CASE_REFERENCE, paperApplication + 4 address fields
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID)
        ));

        assertAddressIsTransformed(
                (Map) transformedCaseData.get("solicitorAddress"),
                ImmutableMap.of(
                        "AddressLine1", "Street",
                        "AddressTown", "London",
                        "AddressPostcode", "SW989SD",
                        "AddressCounty", "Great London",
                        "AddressCountry", "UK"
                )
        );

        assertAddressIsTransformed(
                (Map) transformedCaseData.get("applicantAddress"),
                ImmutableMap.of(
                        "AddressLine1", "Road",
                        "AddressTown", "Manchester",
                        "AddressPostcode", "SW9 9SD",
                        "AddressCounty", "There",
                        "AddressCountry", "Germany"
                )
        );

        assertAddressIsTransformed(
                (Map) transformedCaseData.get("respondentAddress"),
                ImmutableMap.of(
                        "AddressLine1", "Avenue",
                        "AddressTown", "Bristol",
                        "AddressPostcode", "SW1 9SD",
                        "AddressCounty", "Here",
                        "AddressCountry", "France"
                )
        );

        assertAddressIsTransformed(
                (Map) transformedCaseData.get("rSolicitorAddress"),
                ImmutableMap.of(
                        "AddressLine1", "Drive",
                        "AddressTown", "Leeds",
                        "AddressPostcode", "SW9 USB",
                        "AddressCounty", "Where",
                        "AddressCountry", "Scotland"
                )
        );
    }

    @Test
    public void shouldSetOrderForChildrenQuestion1ToYesIfOrderForChildrenFieldIsPopulated() {
        Map<String, Object> optionOneTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
                singletonList(new OcrDataField("OrderForChildren",
                        "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children"))));
        assertThat(optionOneTransformedData, allOf(
                aMapWithSize(4),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry("natureOfApplication5b", "FR_nature_of_application_1"),
                hasEntry("orderForChildrenQuestion1", "Yes")
        ));

        Map<String, Object> optionTwoTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
                singletonList(new OcrDataField("OrderForChildren",
                        "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children"))));
        assertThat(optionTwoTransformedData, allOf(
                aMapWithSize(4),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry("natureOfApplication5b", "FR_nature_of_application_2"),
                hasEntry("orderForChildrenQuestion1", "Yes")
        ));

        Map<String, Object> optionThreeTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
                singletonList(new OcrDataField("OrderForChildren", "there is no agreement, but the applicant is applying for payments"))));
        assertThat(optionThreeTransformedData, allOf(
                aMapWithSize(4),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry("natureOfApplication5b", "FR_nature_of_application_3"),
                hasEntry("orderForChildrenQuestion1", "Yes")
        ));
    }

    @Test
    public void shouldNotSetOrderForChildrenQuestion1IfOrderForChildrenFieldIsNotPopulated() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
                new OcrDataField("OrderForChildren", "")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
                aMapWithSize(3),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry("natureOfApplication5b", ""),
                not(hasKey("orderForChildrenQuestion1"))
        ));
    }

    @Test
    public void shouldTransformOrderForChildrenNoAgreementReasons() {
        ExceptionRecord exceptionRecord =
                createExceptionRecord(singletonList(new OcrDataField("OrderForChildrenNoAgreement", "for a stepchild or stepchildren, "
                        + "in addition to child support maintenance already paid under a Child Support Agency assessment, "
                        + "to meet expenses arising from a child’s disability, "
                        + "to meet expenses incurred by a child in being educated or training for work, "
                        + "when either the child or the person with care of the child or the absent parent of the "
                        + "child is not habitually resident in the United Kingdom"
                )));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat((List<String>) transformedCaseData.get("natureOfApplication6"), hasItems(
                "Step Child or Step Children",
                "In addition to child support",
                "disability expenses",
                "training",
                "When not habitually resident"
        ));
    }

    @Test
    public void shouldTransform_ProvisionMadeFor() {
        Map<String, Object> transformedCaseDataOptionOne = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.PROVISION_MADE_FOR,
                        "in connection with matrimonial or civil partnership proceedings"))));
        assertThat(transformedCaseDataOptionOne.get("provisionMadeFor"), is("matrimonialOrCivilPartnershipProceedings"));

        Map<String, Object> transformedCaseDataOptionTwo = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.PROVISION_MADE_FOR,
                        "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"))));
        assertThat(transformedCaseDataOptionTwo.get("provisionMadeFor"), is("childrenAct1989"));
    }

    @Test
    public void shouldTransform_ApplicantRepresentPaper() {
        Map<String, Object> transformedCaseDataOptionOne = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                        "I am not represented by a solicitor in these proceedings"))));
        assertThat(transformedCaseDataOptionOne.get("applicantRepresentPaper"), is("FR_applicant_represented_1"));

        Map<String, Object> transformedCaseDataOptionTwo = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                        "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor"))));
        assertThat(transformedCaseDataOptionTwo.get("applicantRepresentPaper"), is("FR_applicant_represented_2"));

        Map<String, Object> transformedCaseDataOptionThree = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                        "I am represented by a solicitor in these proceedings, who has signed Section 5, and all "
                                + "documents for my attention should be sent to my solicitor whose details are as follows"))));
        assertThat(transformedCaseDataOptionThree.get("applicantRepresentPaper"), is("FR_applicant_represented_3"));
    }

    @Test
    public void shouldTransformEmptyAuthorisationSignedToNo() {
        assertOnSingleFieldTransformationResult(
                OcrFieldName.AUTHORISATION_SIGNED, "",
                "authorisationSigned", NO_VALUE);
    }

    @Test
    public void shouldTransformAnyAuthorisationSignedToYes() {
        assertOnSingleFieldTransformationResult(
                OcrFieldName.AUTHORISATION_SIGNED, "Any non-empty value should become yes",
                "authorisationSigned", YES_VALUE);
    }

    private void assertOnSingleFieldTransformationResult(String ocrFieldName, String ocrFieldValue, String ccdFieldName, String ccdFieldValue) {
        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(
                createExceptionRecord(asList(new OcrDataField(ocrFieldName, ocrFieldValue))));

        assertThat(transformedCaseData, allOf(
                aMapWithSize(3),
                hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
                hasEntry(PAPER_APPLICATION, YES_VALUE),
                hasEntry(ccdFieldName, ccdFieldValue)
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id(TEST_CASE_ID).ocrDataFields(ocrDataFields).build();
    }

    private void assertAddressIsTransformed(Map<String, Object> address, Map<String, String> sourceFieldAndValueMap) {
        Map<String, String> sourceSuffixToTargetMap = ImmutableMap.of(
                "AddressLine1", "AddressLine1",
                "Town", "PostTown",
                "PostCode", "PostCode",
                "County", "County",
                "Country", "Country"
        );

        BiFunction<Map<String, String>, String, String> getValueForSuffix = (map, suffix) -> map.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().endsWith(suffix.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Expected to find key with suffix %s in map", suffix)))
                .getValue();

        sourceSuffixToTargetMap
                .forEach((key, value) -> assertThat(address.get(value), is(getValueForSuffix.apply(sourceFieldAndValueMap, key))));
    }

    private void assertChildrenInfo(Map<String, Object> transformedCaseData) {
        List<Map<String, Object>> children = (List)(transformedCaseData.get("childrenInfo"));

        assertChild((Map)(children.get(0)).get("value"), asList("Johny Bravo", "2000-03-12", "male", "son", "SON","New Zeeland"));
        assertChild((Map)(children.get(1)).get("value"), asList("Anne Shirley", "1895-03-12", "female", "daughter", "Daughter","Canada"));
    }

    private void assertChild(Map<String, Object> child, List<String> values) {
        assertThat(child, allOf(
                hasEntry("name", values.get(0)),
                hasEntry("dateOfBirth", values.get(1)),
                hasEntry("gender", values.get(2)),
                hasEntry("relationshipToApplicant", values.get(3)),
                hasEntry("relationshipToRespondent", values.get(4)),
                hasEntry("countryOfResidence", values.get(5))
        ));
    }
}
