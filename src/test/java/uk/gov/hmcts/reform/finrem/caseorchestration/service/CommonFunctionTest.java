package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresented;

public class CommonFunctionTest {

    private static String F_NAME = "f";
    private static String L_NAME = "l";

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyReturnsTrue() {
        asList(
                asList("102 Petty France", "SW8 2PX"),
                asList("102 Petty France", "invalid format of postcode")
        ).forEach(testCase -> assertThat(addressLineOneAndPostCodeAreBothNotEmpty(address(testCase)), is(true)));
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyReturnsFalse() {
        asList(
                asList("", ""),
                asList("Street", ""),
                asList("", "SW2 9IP")
        ).forEach(testCase -> assertThat(addressLineOneAndPostCodeAreBothNotEmpty(address(testCase)), is(false)));
    }

    @Test
    public void isApplicantRepresentedShouldReturnTrue() {
        assertThat(isApplicantRepresented(caseData("Yes")), is(true));
    }

    @Test
    public void isApplicantRepresentedShouldReturnFalse() {
        asList(
                "no",
                "234rt3egw43t2wgr42t",
                null,
                ""
        ).forEach(v -> assertThat(isApplicantRepresented(caseData(v)), is(false)));
    }

    @Test
    public void buildFullNameShouldBuildFullName() {
        assertThat(buildFullName(fullName("Pit", "Smith"), F_NAME, L_NAME), is("Pit Smith"));
        assertThat(buildFullName(fullName("", "Smith"), F_NAME, L_NAME), is("Smith"));
        assertThat(buildFullName(fullName("Pit Adam", "Smith"), F_NAME, L_NAME), is("Pit Adam Smith"));
        assertThat(buildFullName(fullName("Pit", "Smith-Johnson"), F_NAME, L_NAME), is("Pit Smith-Johnson"));
        assertThat(buildFullName(fullName("Pit JK", "Smith"), F_NAME, L_NAME), is("Pit JK Smith"));
        assertThat(buildFullName(fullName("Pit", ""), F_NAME, L_NAME), is("Pit"));
        assertThat(buildFullName(fullName("", ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName(null, ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("", null), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("     ", "    "), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("    Pit   ", "     Smith    "), F_NAME, L_NAME), is("Pit Smith"));
    }

    private static Map<String, Object> address(List<String> values) {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("AddressLine1", values.get(0));
        addressMap.put("PostCode", values.get(1));

        return addressMap;
    }

    private static Map<String, Object> caseData(String value) {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_REPRESENTED, value);

        return caseDataMap;
    }

    private static Map<String, Object> fullName(String firstName, String lastName) {
        Map<String, Object> fullNameMap = new HashMap<>();
        fullNameMap.put(F_NAME, firstName);
        fullNameMap.put(L_NAME, lastName);

        return fullNameMap;
    }
}
