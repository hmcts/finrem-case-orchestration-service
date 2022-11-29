package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

public class PBAOrganisationResponseTest {

    private String json = "{\n \"organisationEntityResponse\": {\n"
        + "    \"organisationIdentifier\": \"LY7RZOE\",\n"
        + "    \"name\": \"TestOrg1\",\n"
        + "    \"status\": \"ACTIVE\",\n"
        + "    \"sraId\": \"1111\",\n"
        + "    \"sraRegulated\": true,\n"
        + "    \"companyNumber\": \"1110111\",\n"
        + "    \"companyUrl\": \"http://testorg2.co.uk\",\n"
        + "    \"superUser\": {\n"
        + "      \"firstName\": \"Henry\",\n"
        + "      \"lastName\": \"Harper\",\n"
        + "      \"email\": \"henry_fr_harper@yahoo.com\"\n"
        + "    },\n"
        + "    \"paymentAccount\": [\n"
        + "      \"NUM1\",\n"
        + "      \"NUM2\"\n"
        + "    ],\n"
        + "\"contactInformation\" : []"
        + "  }\n"
        + "}";

    private PBAOrganisationResponse pbaOrganisationResponse;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        pbaOrganisationResponse = mapper.readValue(json, PBAOrganisationResponse.class);
    }

    @Test
    public void shouldPopulateData() {
        assertThat(pbaOrganisationResponse.getOrganisationEntityResponse(), notNullValue());
        OrganisationEntityResponse organisationEntityResponse = pbaOrganisationResponse.getOrganisationEntityResponse();
        assertThat(organisationEntityResponse.getName(), Is.is("TestOrg1"));
        assertThat(organisationEntityResponse.getOrganisationIdentifier(), Is.is("LY7RZOE"));
        assertThat(organisationEntityResponse.getStatus(), Is.is("ACTIVE"));
        assertThat(organisationEntityResponse.getSraId(), Is.is("1111"));
        assertThat(organisationEntityResponse.isSraRegulated(), Is.is(true));
        assertThat(organisationEntityResponse.getCompanyNumber(), Is.is("1110111"));
        assertThat(organisationEntityResponse.getCompanyUrl(), Is.is("http://testorg2.co.uk"));
        assertThat(organisationEntityResponse.getSuperUser(), notNullValue());
        SuperUserResponse superUser = organisationEntityResponse.getSuperUser();
        assertThat(superUser.getEmail(), Is.is("henry_fr_harper@yahoo.com"));
        assertThat(superUser.getFirstName(), Is.is("Henry"));
        assertThat(superUser.getLastName(), Is.is("Harper"));
        assertThat(organisationEntityResponse.getPaymentAccount(), hasItems("NUM1", "NUM2"));
    }
}