package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

public class CommonFunctionTest {

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

    private static Map<String, String> createAddressObject(List<? extends Object> data) {
        Map<String, String> address = new HashMap<>();

        address.put("AddressLine1", (String) data.get(0));
        address.put("PostCode", (String) data.get(1));

        return address;
    }
}
