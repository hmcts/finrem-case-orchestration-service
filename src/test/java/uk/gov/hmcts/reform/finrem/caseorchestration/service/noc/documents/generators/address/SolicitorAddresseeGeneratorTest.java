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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddresseeGeneratorTest {

    protected static final Map<String, Object> SOLICITOR_ADDRESS_VALUE = Map.of("AddressLine1", "Contested address line 1");
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String REPRESENTATIVE_NAME = "representativeName";
    protected static final String ORGANISATION_ID = "organisationId";
    protected static final String ORGANISATION_NAME = "organisationName";

    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private InternationalPostalService postalService;

    @InjectMocks
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails;
    private ChangedRepresentative changedRepresentative;

    @Before
    public void setUpData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, SOLICITOR_ADDRESS_VALUE);
        caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
        changedRepresentative = ChangedRepresentative.builder().name(REPRESENTATIVE_NAME)
            .organisation(Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ORGANISATION_NAME).build()).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedSolicitorsAddressee() {
        when(documentHelper.formatAddressForLetterPrinting(SOLICITOR_ADDRESS_VALUE, false)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative, "applicant");
        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
        verify(postalService).isRecipientResideOutsideOfUK(caseDetails.getData(), "applicant");
    }

}
