package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

@Component
@Slf4j
public class SelectablePartiesCorrespondenceService {

    public void setPartiesToReceiveCorrespondence(FinremCaseData data) {
        List<String> selectedParties = data.getSelectedParties();
        if (selectedParties != null && !selectedParties.isEmpty()) {
            log.info("Setting parties to receive correspondence {} on case {}", selectedParties, data.getCcdCaseId());
            data.setApplicantCorrespondenceEnabled(
                isCorrespondenceShareableWithParties(selectedParties,
                    List.of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.APP_BARRISTER.getCcdCode())));
            data.setRespondentCorrespondenceEnabled(
                isCorrespondenceShareableWithParties(selectedParties,
                    List.of(CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.RESP_BARRISTER.getCcdCode())));
            data.getIntervenerOneWrapper()
                .setIntervenerCorrespondenceEnabled(
                    isCorrespondenceShareableWithParties(selectedParties,
                        List.of(CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_BARRISTER_1.getCcdCode())));
            data.getIntervenerTwoWrapper()
                .setIntervenerCorrespondenceEnabled(
                    isCorrespondenceShareableWithParties(selectedParties,
                        List.of(CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_BARRISTER_2.getCcdCode())));
            data.getIntervenerThreeWrapper()
                .setIntervenerCorrespondenceEnabled(
                    isCorrespondenceShareableWithParties(selectedParties,
                        List.of(CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_BARRISTER_3.getCcdCode())));
            data.getIntervenerFourWrapper()
                .setIntervenerCorrespondenceEnabled(
                    isCorrespondenceShareableWithParties(selectedParties,
                        List.of(CaseRole.INTVR_SOLICITOR_4.getCcdCode(), CaseRole.INTVR_BARRISTER_4.getCcdCode())));
        }
    }

    public boolean isCorrespondenceShareableWithParties(List<String> selectedParties, List<String> partyRoles) {
        for (String party : partyRoles) {
            if (selectedParties.contains(party)) {
                return true;
            }
        }
        return false;
    }
}
