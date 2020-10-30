package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class UpdateSolicitorDetailsServiceTest extends BaseServiceTest {

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    @Autowired UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean PrdOrganisationService prdOrganisationService;

    @Test
    public void whenUpdateSolicitorAddressIsCalled_thenOrganisationResponseIsConvertedToMap() {
        mockPrdOrganisationService();

        Map<String, Object> map = updateSolicitorDetailsService.updateApplicantSolicitorAddressFromPrd(AUTH_TOKEN);

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("AddressLine1", ADDRESS_LINE_1)
            .put("AddressLine2", ADDRESS_LINE_2)
            .put("AddressLine3", ADDRESS_LINE_3)
            .put("County", COUNTY)
            .put("Country", COUNTRY)
            .put("PostTown", TOWN_CITY)
            .put("PostCode", POSTCODE)
            .build();

        expectedMap.forEach((key, value) -> assertThat(map, hasEntry(key, value)));
        assertThat(expectedMap, aMapWithSize(expectedMap.size()));
    }

    private void mockPrdOrganisationService() {
        OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
            .addressLine1(ADDRESS_LINE_1)
            .addressLine2(ADDRESS_LINE_2)
            .addressLine3(ADDRESS_LINE_3)
            .county(COUNTY)
            .country(COUNTRY)
            .townCity(TOWN_CITY)
            .postcode(POSTCODE)
            .build();
        when(prdOrganisationService.retrieveOrganisationsData(eq(AUTH_TOKEN))).thenReturn(OrganisationsResponse.builder()
            .contactInformation(organisationContactInformation).build());
    }
}
