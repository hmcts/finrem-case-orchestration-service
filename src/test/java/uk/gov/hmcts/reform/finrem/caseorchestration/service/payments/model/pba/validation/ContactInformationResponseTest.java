package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class ContactInformationResponseTest {
    private String json = "{"
        + "        \"addressLine1\": \"addressLine1\",\n"
        + "        \"addressLine2\": \"addressLine2\",\n"
        + "        \"addressLine3\": \"addressLine3\",\n"
        + "        \"country\": \"country\",\n"
        + "        \"county\": \"county\",\n"
        + "        \"dxAddress\": [\n"
        + "          {\n"
        + "            \"dxExchange\": \"string\",\n"
        +
        "            \"dxNumber\": \"string\"\n"
        + "          }\n"
        + "        ],\n"
        + "        \"postCode\": \"postCode\",\n"
        + "        \"townCity\": \"townCity\"\n"
        + "      }\n"
        + "    }";

    private ContactInformationResponse contactInformation;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        contactInformation = objectMapper.readValue(json, ContactInformationResponse.class);
    }

    @Test
    public void shouldPopulateData() {
        assertThat(contactInformation.getAddressLine1(), is("addressLine1"));
        assertThat(contactInformation.getAddressLine2(), is("addressLine2"));
        assertThat(contactInformation.getAddressLine3(), is("addressLine3"));
        assertThat(contactInformation.getCountry(), is("country"));
        assertThat(contactInformation.getCounty(), is("county"));
        assertThat(contactInformation.getPostCode(), is("postCode"));
        assertThat(contactInformation.getTownCity(), is("townCity"));
        assertThat(contactInformation.getDxAddress(), notNullValue());
    }
}