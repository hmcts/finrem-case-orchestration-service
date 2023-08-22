package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SelectablePartiesCorrespondenceServiceTest {

    SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService = new SelectablePartiesCorrespondenceService();


    FinremCaseData finremCaseData;

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
