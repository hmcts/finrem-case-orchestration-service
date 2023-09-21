package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SelectablePartiesCorrespondenceServiceTest {

    SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;
    FinremCaseData finremCaseData;

    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setUpTest() {
        selectablePartiesCorrespondenceService = new SelectablePartiesCorrespondenceService(
            finremCaseDetailsMapper);
    }

    @Test
    public void shouldSetAllPartiesToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(fullPartyList())).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertTrue(finremCaseData.isApplicantCorrespondenceEnabled());
        assertTrue(finremCaseData.isRespondentCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldOnlySetApplicantPartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.APP_SOLICITOR.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertTrue(finremCaseData.isApplicantCorrespondenceEnabled());
        assertFalse(finremCaseData.isRespondentCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }

    @Test
    public void shouldOnlySetRespondentPartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.RESP_BARRISTER.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertFalse(finremCaseData.isApplicantCorrespondenceEnabled());
        assertTrue(finremCaseData.isRespondentCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldOnlySetIntervenerOnePartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_SOLICITOR_1.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertFalse(finremCaseData.isApplicantCorrespondenceEnabled());
        assertFalse(finremCaseData.isRespondentCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldOnlySetIntervenerTwoPartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_SOLICITOR_2.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertFalse(finremCaseData.isApplicantCorrespondenceEnabled());
        assertFalse(finremCaseData.isRespondentCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldOnlySetIntervenerThreePartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_SOLICITOR_3.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertFalse(finremCaseData.isApplicantCorrespondenceEnabled());
        assertFalse(finremCaseData.isRespondentCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldOnlySetIntervenerFourPartyToReceiveCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_SOLICITOR_4.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);

        assertFalse(finremCaseData.isApplicantCorrespondenceEnabled());
        assertFalse(finremCaseData.isRespondentCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled());
        assertFalse(finremCaseData.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled());
        assertTrue(finremCaseData.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled());
    }


    @Test
    public void shouldDetermineIfApplicantCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.APP_SOLICITOR.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendApplicantCorrespondence(caseDetails));
    }


    @Test
    public void shouldDetermineIfRespondentCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.RESP_SOLICITOR.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendRespondentCorrespondence(caseDetails));
    }

    @Test
    public void shouldDetermineIfIntervenerOneCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_BARRISTER_1.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendIntervenerOneCorrespondence(caseDetails));
    }

    @Test
    public void shouldDetermineIfIntervenerTwoCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_BARRISTER_2.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendIntervenerTwoCorrespondence(caseDetails));
    }

    @Test
    public void shouldDetermineIfIntervenerThreeCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_BARRISTER_3.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendIntervenerThreeCorrespondence(caseDetails));
    }

    @Test
    public void shouldDetermineIfIntervenerFourCorrespondenceShouldBeSent() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_BARRISTER_4.getCcdCode()))).build();

        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails))
            .thenReturn(FinremCaseDetails.builder().data(finremCaseData).build());

        assertTrue(selectablePartiesCorrespondenceService.shouldSendIntervenerFourCorrespondence(caseDetails));
    }

    @Test
    public void shouldValidateListingNoticeCorrespondence() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.INTVR_BARRISTER_4.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);
        List<String> errors = selectablePartiesCorrespondenceService.validateCorrespondenceFieldsForListingNoticeEvent(finremCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("This listing notice must be sent to the applicant and respondent as default."
            + " If this listing needs to be sent to only one of these parties please use the general order event."));
    }

    @Test
    public void shouldValidateListingNoticeCorrespondenceWithNoErrors() {

        finremCaseData = FinremCaseData.builder().partiesOnCase(buildDynamicSelectableParties(of(CaseRole.RESP_SOLICITOR.getCcdCode(),
            CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.INTVR_BARRISTER_1.getCcdCode()))).build();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseData);
        List<String> errors = selectablePartiesCorrespondenceService.validateCorrespondenceFieldsForListingNoticeEvent(finremCaseData);
        assertThat(errors.size(), is(0));
    }

    private DynamicMultiSelectList buildDynamicSelectableParties(List<String> parties) {

        List<DynamicMultiSelectListElement> list = new ArrayList<>();
        parties.forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(list)
            .listItems(list)
            .build();
    }

    private List<String> fullPartyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.APP_BARRISTER.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.RESP_BARRISTER.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_BARRISTER_1.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_BARRISTER_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_BARRISTER_3.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_4.getCcdCode(), CaseRole.INTVR_BARRISTER_4.getCcdCode());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }
}
