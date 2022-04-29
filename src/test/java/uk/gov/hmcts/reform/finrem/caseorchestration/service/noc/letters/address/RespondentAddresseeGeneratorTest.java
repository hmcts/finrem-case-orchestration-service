package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.address;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.address.RespondentAddresseeGenerator;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;

@RunWith(MockitoJUnitRunner.class)
public class RespondentAddresseeGeneratorTest {

    protected static final String CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE = "Consented Respondent first middle name";
    protected static final String CONSENTED_RESPONDENT_LAST_NAME_VALUE = "Consented Respondent last name";
    protected static final String CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE = "Contested Respondent first middle name";
    protected static final String CONTESTED_RESPONDENT_LAST_NAME_VALUE = "Contested Respondent last name";
    protected static final Map RESPONDENT_ADDRESS_VALUE = Map.of("AddressLine1", "Consented address line 1");
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String CONTESTED_FULL_NAME = "contestedFullName";
    protected static final String CONSESNED_FULL_NAME = "consentedFullName";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    RespondentAddresseeGenerator respondentAddresseeGenerator;

    CaseDetails caseDetails;
    private Map<String, Object> caseData;

    @Before
    public void setUpData() {
        caseData = Map.of(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE, CONSENTED_RESPONDENT_LAST_NAME,
            CONSENTED_RESPONDENT_LAST_NAME_VALUE, CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE,
            CONTESTED_RESPONDENT_LAST_NAME, CONTESTED_RESPONDENT_LAST_NAME_VALUE, RESPONDENT_ADDRESS, RESPONDENT_ADDRESS_VALUE);
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedRespondentAddressee() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(caseDataService.buildFullName(caseData, CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, CONTESTED_RESPONDENT_LAST_NAME)).thenReturn(
            CONTESTED_FULL_NAME);
        when(documentHelper.formatAddressForLetterPrinting(RESPONDENT_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = respondentAddresseeGenerator.generate(caseDetails);

        assertThat(addressee.getName(), is(CONTESTED_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

    @Test
    public void whenContestedCaseShouldBuildContestedRespondentAddressee() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);
        when(caseDataService.buildFullName(caseData, CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, CONSENTED_RESPONDENT_LAST_NAME)).thenReturn(
            CONSESNED_FULL_NAME);
        when(documentHelper.formatAddressForLetterPrinting(RESPONDENT_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = respondentAddresseeGenerator.generate(caseDetails);

        assertThat(addressee.getName(), is(CONSESNED_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }
}
