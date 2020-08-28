package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ContactDetailsMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

public class ContactDetailsMapperTest {

    public static final String LINE_1 = "AddressLine1";
    public static final String LINE_2 = "AddressLine2";
    public static final String TOWN = "AddressTown";
    public static final String POSTCODE = "AddressPostcode";
    public static final String COUNTY = "AddressCounty";
    public static final String COUNTRY = "AddressCountry";
    public static final String TEST_EMAIL = "solicitor@mail.com";
    public static final String TEST_PHONE = "077654567";

    @Test
    public void applyAddressesMappingsShouldMapAllAddresses() {
        List<OcrDataField> ocrFields = getOcrFieldsForAddresses();
        Map<String, Object> data = new HashMap<>();

        ContactDetailsMapper.applyAddressesMappings(ocrFields, data);

        assertTransformationForAddressIsValid(
                data,
                ContactDetailsMapper.CcdFields.APPLICANT,
                ContactDetailsMapper.CcdFields.RESPONDENT,
                ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
                ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR
        );
    }

    @Test
    public void setupContactDetailsForApplicantAndRespondentForRepresentedBothCitizens() {
        Map<String, Object> data = new HashMap<>();
        data.put(ContactDetailsMapper.CcdFields.APPLICANT, buildMappedAddress1());
        data.put(ContactDetailsMapper.CcdFields.RESPONDENT, buildMappedAddress2());
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_EMAIL, TEST_EMAIL);
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_PHONE, TEST_PHONE);
        data.put(CCDConfigConstant.APPLICANT_REPRESENTED, YES_VALUE);
        data.put(CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);

        ContactDetailsMapper.setupContactDetailsForApplicantAndRespondent(data);

        assertTransformationForAddressIsValid(
                data,
                ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
                ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR,
                ContactDetailsMapper.CcdFields.APPLICANT,
                ContactDetailsMapper.CcdFields.RESPONDENT
        );

        assertThat(nullToEmpty(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_EMAIL)), is(TEST_EMAIL));
        assertThat(nullToEmpty(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_PHONE)), is(TEST_PHONE));
    }

    @Test
    public void setupContactDetailsForApplicantAndRespondentForOnlyApplicantRepresented() {
        Map<String, Object> data = new HashMap<>();
        data.put(ContactDetailsMapper.CcdFields.APPLICANT, buildMappedAddress1());
        data.put(ContactDetailsMapper.CcdFields.RESPONDENT, buildMappedAddress2());
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_EMAIL, "solicitor@mail.com");
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_PHONE, "077654567");
        data.put(CCDConfigConstant.APPLICANT_REPRESENTED, YES_VALUE);

        ContactDetailsMapper.setupContactDetailsForApplicantAndRespondent(data);

        assertTransformationForAddressIsValid(
                data,
                ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
                ContactDetailsMapper.CcdFields.RESPONDENT,
                ContactDetailsMapper.CcdFields.APPLICANT,
                ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR
        );

        assertThat(nullToEmpty(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_EMAIL)), is(TEST_EMAIL));
        assertThat(nullToEmpty(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_PHONE)), is(TEST_PHONE));
    }

    @Test
    public void setupContactDetailsForApplicantAndRespondentForOnlyRespondentRepresented() {
        Map<String, Object> data = new HashMap<>();
        data.put(ContactDetailsMapper.CcdFields.APPLICANT, buildMappedAddress1());
        data.put(ContactDetailsMapper.CcdFields.RESPONDENT, buildMappedAddress2());
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_EMAIL, "solicitor@mail.com");
        data.put(ContactDetailsMapper.CcdFields.APPLICANT_PHONE, "077654567");
        data.put(CCDConfigConstant.APPLICANT_REPRESENTED, NO_VALUE);
        data.put(CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);

        ContactDetailsMapper.setupContactDetailsForApplicantAndRespondent(data);

        assertTransformationForAddressIsValid(
                data,
                ContactDetailsMapper.CcdFields.APPLICANT,
                ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR,
                ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
                ContactDetailsMapper.CcdFields.RESPONDENT
        );

        assertNull(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_EMAIL));
        assertNull(data.get(ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR_PHONE));
    }

    public static ImmutableMap<String, String> buildImmutableMap(
            String k1, String v1,
            String k2, String v2,
            String k3, String v3,
            String k4, String v4,
            String k5, String v5,
            String k6, String v6) {

        Map<String, String> result = new HashMap<>();
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        result.put(k4, v4);
        result.put(k5, v5);
        result.put(k6, v6);

        return ImmutableMap.copyOf(result);
    }

    public static List<OcrDataField> getOcrFieldsForAddresses() {
        return new ArrayList<>(Arrays.asList(
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_LINE_1, "Road"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_LINE_2, "House"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_TOWN, "Manchester"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_COUNTY, "There"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_POSTCODE, "SW9 9SD"),
                new OcrDataField(OcrFieldName.APPLICANT_ADDRESS_COUNTRY, "Germany"),

                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_LINE_1, "Avenue"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_LINE_2, "Bungalow"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_TOWN, "Bristol"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_COUNTY, "Here"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_POSTCODE, "SW1 9SD"),
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_COUNTRY, "France")
        ));
    }

    public static void assertAddressIsTransformed(Map<String, Object> address, Map<String, String> sourceFieldAndValueMap) {
        Map<String, String> sourceSuffixToTargetMap = buildImmutableMap(
                LINE_1, "AddressLine1",
                LINE_2, "AddressLine2",
                TOWN, "PostTown",
                POSTCODE, "PostCode",
                COUNTY, "County",
                COUNTRY, "Country"
        );

        BiFunction<Map<String, String>, String, String> getValueForSuffix = (map, suffix) -> map.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().endsWith(suffix.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Expected to find key with suffix %s in map", suffix)))
                .getValue();

        sourceSuffixToTargetMap
                .forEach((key, value) -> assertThat(address.get(value), is(getValueForSuffix.apply(sourceFieldAndValueMap, key))));
    }

    public static void assertTransformationForAddressIsValid(
            Map<String, Object> transformedCaseData,
            String expectedApplicantPopulatedField,
            String expectedRespondentPopulatedField,
            String expectedApplicantEmptyField,
            String expectedRespondentEmptyField) {

        assertAddressIsTransformed(
                (Map) transformedCaseData.get(expectedApplicantPopulatedField),
                buildAddress1()
        );

        assertAddressIsTransformed(
                (Map) transformedCaseData.get(expectedRespondentPopulatedField),
                buildAddress2()

        );

        assertNull(transformedCaseData.get(expectedApplicantEmptyField));
        assertNull(transformedCaseData.get(expectedRespondentEmptyField));
    }

    public static OcrDataField ocrDataFieldIndicatingApplicantIsRepresented() {
        return new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                "I am represented by a solicitor in these proceedings, who has signed Section 5 and all "
                        + "documents for my attention should be sent to my solicitor whose details are as follows");
    }

    private static ImmutableMap<String, String> buildAddress1() {
        return buildImmutableMap(
                "AddressLine1", "Road",
                "AddressLine2", "House",
                "AddressTown", "Manchester",
                "AddressPostcode", "SW9 9SD",
                "AddressCounty", "There",
                "AddressCountry", "Germany"
        );
    }

    private static ImmutableMap<String, String> buildAddress2() {
        return buildImmutableMap(
                "AddressLine1", "Avenue",
                "AddressLine2", "Bungalow",
                "AddressTown", "Bristol",
                "AddressPostcode", "SW1 9SD",
                "AddressCounty", "Here",
                "AddressCountry", "France"
        );
    }

    private static ImmutableMap<String, String> buildMappedAddress1() {
        return buildImmutableMap(
                "AddressLine1", "Road",
                "AddressLine2", "House",
                "PostTown", "Manchester",
                "PostCode", "SW9 9SD",
                "County", "There",
                "Country", "Germany"
        );
    }

    private static ImmutableMap<String, String> buildMappedAddress2() {
        return buildImmutableMap(
                "AddressLine1", "Avenue",
                "AddressLine2", "Bungalow",
                "PostTown", "Bristol",
                "PostCode", "SW1 9SD",
                "County", "Here",
                "Country", "France"
        );
    }
}
