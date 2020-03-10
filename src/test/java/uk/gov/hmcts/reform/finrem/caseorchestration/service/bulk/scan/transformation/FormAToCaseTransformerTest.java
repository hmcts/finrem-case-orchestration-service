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
            new OcrDataField("ApplicantRepresented", "I am not represented by a solicitor in these proceedings")
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
            hasEntry("applicantRepresentPaper", "I am not represented by a solicitor in these proceedings")
        ));
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
            new OcrDataField("ApplicantRepresented", "I am not represented by a solicitor in these proceedings"),

            new OcrDataField("ApplicantSolicitorAddressLine1", "Street"),
            new OcrDataField("ApplicantSolicitorAddressTown", "London"),
            new OcrDataField("ApplicantSolicitorAddressCounty", "Great London"),
            new OcrDataField("ApplicantSolicitorAddressPostcode", "SW989SD"),
            new OcrDataField("ApplicantSolicitorAddressCountry", "UK"),

            new OcrDataField("ApplicantAddressLine1", "Road"),
            new OcrDataField("ApplicantAddressTown", "Manchester"),
            new OcrDataField("ApplicantAddressCounty", "There"),
            new OcrDataField("ApplicantAddressPostcode", "SW9 9SD"),
            new OcrDataField("ApplicantAddressCountry", "Germany"),

            new OcrDataField("RespondentAddressLine1", "Avenue"),
            new OcrDataField("RespondentAddressTown", "Bristol"),
            new OcrDataField("RespondentAddressCounty", "Here"),
            new OcrDataField("RespondentAddressPostcode", "SW1 9SD"),
            new OcrDataField("RespondentAddressCountry", "France"),

            new OcrDataField("RespondentSolicitorAddressLine1", "Drive"),
            new OcrDataField("RespondentSolicitorAddressTown", "Leeds"),
            new OcrDataField("RespondentSolicitorAddressCounty", "Where"),
            new OcrDataField("RespondentSolicitorAddressPostcode", "SW9 USB"),
            new OcrDataField("RespondentSolicitorAddressCountry", "Scotland")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(6),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry("applicantRepresentPaper", "I am not represented by a solicitor in these proceedings")
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

    @Test
    public void shouldMapPhoneNumber() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(Arrays.asList(
            new OcrDataField("ApplicantRepresented", "I am not represented by a solicitor in these proceedings"),
            new OcrDataField("ApplicantPhone", "0712345654")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(3),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry("applicantRepresentPaper", "I am not represented by a solicitor in these proceedings"),
            hasEntry("applicantPhone", "0712345654")
        ));
    }

    @Test
    public void shouldMapEmail() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(Arrays.asList(
            new OcrDataField("ApplicantRepresented", "I am not represented by a solicitor in these proceedings"),
            new OcrDataField("ApplicantSolicitorEmail", "test@example.com")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(3),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry("applicantRepresentPaper", "I am not represented by a solicitor in these proceedings"),
            hasEntry("solicitorEmail", "test@example.com")
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
}
