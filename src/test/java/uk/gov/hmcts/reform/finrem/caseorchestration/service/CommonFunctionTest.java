package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

public class CommonFunctionTest {

    private static String F_NAME = "f";
    private static String L_NAME = "l";

    @Test
    public void nullToEmptyShouldReturnEmptyWhenNull() {
        assertThat(nullToEmpty(null), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnEmptyWhenEmpty() {
        assertThat(nullToEmpty(""), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnStringWhenString() {
        assertThat(nullToEmpty("this is my value"), is("this is my value"));
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnTrueWhenLineOneAndPostCodeArePopulated() {
        assertThat(
                addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(asList("London Road", "sw2 3rf"))),
                is(true)
        );
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalseWhenNull() {
        assertThat(addressLineOneAndPostCodeAreBothNotEmpty(null), is(false));
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalse() {
        asList(
                asList("", "sw2 3rf"),
                asList("", ""),
                asList("London Road", ""),
                asList("London Road", null),
                asList(null, null),
                asList(null, "Sw8 7ty")
        ).forEach(data -> assertThat(
                addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(data)),
                is(false))
        );
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

    private static Map<String, String> createAddressObject(List<? extends Object> data) {
        Map<String, String> address = new HashMap<>();

        address.put("AddressLine1", (String) data.get(0));
        address.put("PostCode", (String) data.get(1));

        return address;
    }

    private static Map<String, Object> fullName(String firstName, String lastName) {
        Map<String, Object> fullNameMap = new HashMap<>();
        fullNameMap.put(F_NAME, firstName);
        fullNameMap.put(L_NAME, lastName);

        return fullNameMap;
    }
}
