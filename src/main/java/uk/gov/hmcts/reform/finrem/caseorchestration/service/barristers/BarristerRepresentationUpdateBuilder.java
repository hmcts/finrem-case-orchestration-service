package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Service
@RequiredArgsConstructor
public class BarristerRepresentationUpdateBuilder {

    private final IdamService idamService;

    public record BarristerUpdateParams(FinremCaseData caseData, String authToken, BarristerParty barristerParty,
                                        Barrister barrister) {
    }

    /**
     * Build representation update when a barrister is added to the case.
     *
     * @param barristerUpdateParams the parameters required to build the representation update
     * @return the representation update
     */
    public RepresentationUpdate buildBarristerAdded(BarristerUpdateParams barristerUpdateParams) {
        ChangedRepresentative changedRepresentative = convertToChangedRepresentative(barristerUpdateParams.barrister);
        return build(barristerUpdateParams, changedRepresentative, null);
    }

    /**
     * Build representation update when a barrister is removed from the case.
     *
     * @param barristerUpdateParams the parameters required to build the representation update
     * @return the representation update
     */
    public RepresentationUpdate buildBarristerRemoved(BarristerUpdateParams barristerUpdateParams) {
        ChangedRepresentative changedRepresentative = convertToChangedRepresentative(barristerUpdateParams.barrister);
        return build(barristerUpdateParams, null, changedRepresentative);
    }

    private RepresentationUpdate build(BarristerUpdateParams barristerUpdateParams,
                                       ChangedRepresentative added, ChangedRepresentative removed) {
        return RepresentationUpdate.builder()
            .added(added)
            .removed(removed)
            .by(idamService.getIdamFullName(barristerUpdateParams.authToken))
            .via(MANAGE_BARRISTERS)
            .date(LocalDateTime.now())
            .clientName(getClientName(barristerUpdateParams.caseData, barristerUpdateParams.barristerParty))
            .party(getParty(barristerUpdateParams.barristerParty))
            .build();
    }

    private ChangedRepresentative convertToChangedRepresentative(Barrister barrister) {
        return ChangedRepresentative.builder()
            .name(barrister.getName())
            .email(barrister.getEmail())
            .organisation(barrister.getOrganisation())
            .build();
    }

    private String getClientName(FinremCaseData caseData, BarristerParty barristerParty) {
        return switch (barristerParty) {
            case APPLICANT -> caseData.getFullApplicantName();
            case RESPONDENT -> caseData.getRespondentFullName();
            case INTERVENER1 -> caseData.getIntervenerOne().getIntervenerName();
            case INTERVENER2 -> caseData.getIntervenerTwo().getIntervenerName();
            case INTERVENER3 -> caseData.getIntervenerThree().getIntervenerName();
            case INTERVENER4 -> caseData.getIntervenerFour().getIntervenerName();
        };
    }

    private String getParty(BarristerParty barristerParty) {
        return switch (barristerParty) {
            case APPLICANT -> APPLICANT;
            case RESPONDENT -> RESPONDENT;
            case INTERVENER1, INTERVENER2, INTERVENER3, INTERVENER4 -> INTERVENER;
        };
    }
}
