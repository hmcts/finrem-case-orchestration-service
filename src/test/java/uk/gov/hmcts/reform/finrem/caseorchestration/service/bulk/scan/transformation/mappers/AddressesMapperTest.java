package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation.mappers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.AddressesMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AddressesMapperTest {

    public static final String LINE_1 = "AddressLine1";
    public static final String LINE_2 = "AddressLine2";
    public static final String TOWN = "AddressTown";
    public static final String POSTCODE = "AddressPostcode";
    public static final String COUNTY = "AddressCounty";
    public static final String COUNTRY = "AddressCountry";

    @Test
    public void applyAddressesMappingsShouldMapAllAddresses() {
        List<OcrDataField> ocrFields = getOcrFieldsForAddresses();
        Map<String, Object> data = new HashMap<>();

        AddressesMapper.applyAddressesMappings(ocrFields, data);

        assertAddressIsTransformed(
                (Map) data.get(AddressesMapper.CcdFields.APPLICANT),
                buildImmutableMap(
                        LINE_1, "Road",
                        LINE_2, "House",
                        TOWN, "Manchester",
                        POSTCODE, "SW9 9SD",
                        COUNTY, "There",
                        COUNTRY, "Germany"
                )
        );

        assertAddressIsTransformed(
                (Map) data.get(AddressesMapper.TempCcdFields.APPLICANT_SOLICITOR),
                buildImmutableMap(
                        LINE_1, "Street",
                        LINE_2, "The building",
                        TOWN, "London",
                        POSTCODE, "SW989SD",
                        COUNTY, "Great London",
                        COUNTRY, "UK"
                )
        );

        assertAddressIsTransformed(
                (Map) data.get(AddressesMapper.CcdFields.RESPONDENT),
                buildImmutableMap(
                        LINE_1, "Avenue",
                        LINE_2, "Bungalow",
                        TOWN, "Bristol",
                        POSTCODE, "SW1 9SD",
                        COUNTY, "Here",
                        COUNTRY, "France"
                )
        );

        assertAddressIsTransformed(
                (Map) data.get(AddressesMapper.TempCcdFields.RESPONDENT_SOLICITOR),
                buildImmutableMap(
                        LINE_1, "Drive",
                        LINE_2, "Block of flats",
                        TOWN, "Leeds",
                        POSTCODE, "SW9 USB",
                        COUNTY, "Where",
                        COUNTRY, "Scotland"
                )
        );
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
                new OcrDataField(OcrFieldName.RESPONDENT_ADDRESS_COUNTRY, "France"),

                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_LINE_1, "Street"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_LINE_2, "The building"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_TOWN, "London"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_COUNTY, "Great London"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_POSTCODE, "SW989SD"),
                new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_ADDRESS_COUNTRY, "UK"),

                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_LINE_1, "Drive"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_LINE_2, "Block of flats"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_TOWN, "Leeds"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_COUNTY, "Where"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_POSTCODE, "SW9 USB"),
                new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_ADDRESS_COUNTRY, "Scotland")
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
}
