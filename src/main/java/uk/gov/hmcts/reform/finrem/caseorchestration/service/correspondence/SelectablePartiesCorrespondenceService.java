package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class SelectablePartiesCorrespondenceService {

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

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

    public FinremCaseDetails setPartiesToReceiveCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails;
    }

    public boolean shouldSendApplicantCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().isApplicantCorrespondenceEnabled();
    }

    public boolean shouldSendRespondentCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().isRespondentCorrespondenceEnabled();
    }

    public boolean shouldSendIntervenerOneCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled();
    }

    public boolean shouldSendIntervenerTwoCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled();
    }

    public boolean shouldSendIntervenerThreeCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled();
    }

    public boolean shouldSendIntervenerFourCorrespondence(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        return finremCaseDetails.getData().getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled();
    }

    public boolean isCorrespondenceShareableWithParties(List<String> selectedParties, List<String> partyRoles) {
        for (String party : partyRoles) {
            if (selectedParties.contains(party)) {
                return true;
            }
        }
        return false;
    }


    public List<String> validateApplicantAndRespondentCorrespondenceAreSelected(FinremCaseData data, String errorMessage) {
        List<String> errors = new ArrayList<>();
        if (!data.isApplicantCorrespondenceEnabled() || !data.isRespondentCorrespondenceEnabled()) {
            errors.add(errorMessage);
        }
        return errors;
    }

    public List<String> validateApplicantAndRespondentCorrespondenceAreInSynch(FinremCaseData data, String errorMessage) {
        List<String> errors = new ArrayList<>();
        if (data.isApplicantCorrespondenceEnabled() == data.isRespondentCorrespondenceEnabled()) {
            errors.add(errorMessage);
        }
        return errors;
    }
}
