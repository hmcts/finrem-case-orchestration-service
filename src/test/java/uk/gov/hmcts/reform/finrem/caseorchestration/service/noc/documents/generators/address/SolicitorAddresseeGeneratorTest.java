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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddresseeGeneratorTest {

    protected static final String CONSENTED_SOLICITOR_NAME_VALUE = "Consented Solicitor name";
    protected static final String CONTESTED_SOLICITOR_NAME_VALUE = "Contested Solicitor name";
    protected static final Map CONSENTED_SOLICITOR_ADDRESS_VALUE = Map.of("AddressLine1", "Consented address line 1");
    protected static final Map CONTESTED_SOLICITOR_ADDRESS_VALUE = Map.of("AddressLine1", "Contested address line 1");
    protected static final String FORMATTED_ADDRESS = "formattedAddress";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails;

    @Before
    public void setUpData() {
        Map<String, Object> caseData =
            Map.of(CONSENTED_SOLICITOR_NAME, CONSENTED_SOLICITOR_NAME_VALUE, CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_NAME_VALUE,
                CONSENTED_SOLICITOR_ADDRESS, CONSENTED_SOLICITOR_ADDRESS_VALUE, CONTESTED_SOLICITOR_ADDRESS, CONTESTED_SOLICITOR_ADDRESS_VALUE);
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedSolicitorsAddressee() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(documentHelper.formatAddressForLetterPrinting(CONTESTED_SOLICITOR_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails);

        assertThat(addressee.getName(), is(CONTESTED_SOLICITOR_NAME_VALUE));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

    @Test
    public void whenContestedCaseShouldBuildContestedSolicitorsAddressee() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);
        when(documentHelper.formatAddressForLetterPrinting(CONSENTED_SOLICITOR_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails);

        assertThat(addressee.getName(), is(CONSENTED_SOLICITOR_NAME_VALUE));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }
}
