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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BULK_SCAN_CASE_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;

public class FormAToCaseTransformerTest {

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
            new OcrDataField(OcrFieldName.APPLICANT_EMAIL, "applicant@divorcity.com")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(CCDConfigConstant.DIVORCE_CASE_NUMBER, "1234567890"),
            hasEntry("HWFNumber", "123456"),
            hasEntry("applicantFMName", "Peter"),
            hasEntry("applicantLName", "Griffin"),
            hasEntry("appRespondentFMname", "Louis"),
            hasEntry("appRespondentLName", "Griffin"),
            hasEntry("provisionMadeFor", "in connection with matrimonial or civil partnership proceedings"),
            hasEntry("applicantIntendsTo", "ApplyToCourtFor"),
            hasEntry("applyingForConsentOrder", "Yes"),
            hasEntry("divorceStageReached", "Decree Nisi"),
            hasEntry("applicantRepresentPaper", "I am not represented by a solicitor in these proceedings"),
            hasEntry(CCDConfigConstant.SOLICITOR_NAME, "Saul Call"),
            hasEntry(CCDConfigConstant.SOLICITOR_FIRM, "Better Divorce Ltd"),
            hasEntry("solicitorPhone", "0712456543"),
            hasEntry("solicitorDXnumber", "DX123"),
            hasEntry("solicitorReference", "SOL-RED"),
            hasEntry(CCDConfigConstant.PBA_NUMBER, "PBA123456"),
            hasEntry(CCDConfigConstant.SOLICITOR_EMAIL, "test@example.com"),
            hasEntry("applicantPhone", "0712345654"),
            hasEntry("applicantEmail", "applicant@divorcity.com")
        ));
        
        assertThat(transformedCaseData.get("natureOfApplication2"), is(asList("Periodical Payment Order", "Pension Attachment Order")));
        assertThat(transformedCaseData.get("dischargePeriodicalPaymentSubstituteFor"), is(asList("Lump Sum Order", "Pension Sharing Order")));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("UnexpectedName", "UnexpectedValue")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID)
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
            aMapWithSize(5),    // 5 = BULK_SCAN_CASE_REFERENCE + 4 address fields
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID)
        ));

        assertAddressIsTransformed(
            (Map)transformedCaseData.get("solicitorAddress"),
            ImmutableMap.of(
            "AddressLine1", "Street",
            "AddressTown", "London",
            "AddressPostcode", "SW989SD",
            "AddressCounty", "Great London",
            "AddressCountry", "UK"
            )
        );

        assertAddressIsTransformed(
            (Map)transformedCaseData.get("applicantAddress"),
            ImmutableMap.of(
                "AddressLine1", "Road",
                "AddressTown", "Manchester",
                "AddressPostcode", "SW9 9SD",
                "AddressCounty", "There",
                "AddressCountry", "Germany"
            )
        );

        assertAddressIsTransformed(
            (Map)transformedCaseData.get("respondentAddress"),
            ImmutableMap.of(
                "AddressLine1", "Avenue",
                "AddressTown", "Bristol",
                "AddressPostcode", "SW1 9SD",
                "AddressCounty", "Here",
                "AddressCountry", "France"
            )
        );

        assertAddressIsTransformed(
            (Map)transformedCaseData.get("rSolicitorAddress"),
            ImmutableMap.of(
                "AddressLine1", "Drive",
                "AddressTown", "Leeds",
                "AddressPostcode", "SW9 USB",
                "AddressCounty", "Where",
                "AddressCountry", "Scotland"
            )
        );
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
}
