package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddresseeGeneratorTest {

    protected static final Map SOLICITOR_ADDRESS_VALUE = Map.of("AddressLine1", "Contested address line 1");
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String REPRESENTATIVE_NAME = "representativeName";
    protected static final String ORGANISATION_ID = "organisationId";
    protected static final String ORGANISATION_NAME = "organisationName";

    @Mock
    private UpdateSolicitorDetailsService solicitorContactDetailsService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails;
    private ChangedRepresentative changedRepresentative;

    @Before
    public void setUpData() {
        caseDetails = CaseDetails.builder().build();
        changedRepresentative = ChangedRepresentative.builder().name(REPRESENTATIVE_NAME)
            .organisation(Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ORGANISATION_NAME).build()).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedSolicitorsAddressee() {

        when(solicitorContactDetailsService.convertOrganisationAddressToSolicitorAddress(
            changedRepresentative.getOrganisation().getOrganisationID())).thenReturn(
            SOLICITOR_ADDRESS_VALUE);
        when(documentHelper.formatAddressForLetterPrinting(SOLICITOR_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);


        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative);

        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

}
