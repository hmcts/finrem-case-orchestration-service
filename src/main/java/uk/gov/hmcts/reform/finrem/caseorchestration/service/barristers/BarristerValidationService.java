package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarristerValidationService {

    private static final Set<String> RESPONDENT_ROLES = Set.of(RESP_SOLICITOR_POLICY, RESPONDENT_BARRISTER_ROLE);
    private static final Set<String> APPLICANT_ROLES = Set.of(APP_SOLICITOR_POLICY, APPLICANT_BARRISTER_ROLE);

    private final PrdOrganisationService organisationService;
    private final AssignCaseAccessService assignCaseAccessService;

    public List<String> validateBarristerEmails(List<BarristerData> barristers,
                                                String authToken,
                                                String caseId,
                                                String caseRole) {
        return Streams.mapWithIndex(
            barristers.stream(),
            (barristerData, index) ->
                performBasicValidation(barristerData.getBarrister(), index, barristers.size(), authToken, caseId, caseRole))
            .flatMap(Collection::stream).toList();
    }

    private List<String> performBasicValidation(Barrister barrister,
                                                long currentRepresentativeIndex,
                                                int sizeOfRepresentatives,
                                                String authToken,
                                                String caseId,
                                                String caseRole) {
        List<String> validationErrors = newArrayList();
        Set<String> opposingRoles = caseRole.equals(APP_SOLICITOR_POLICY) ? RESPONDENT_ROLES : APPLICANT_ROLES;
        Optional<String> userId = organisationService.findUserByEmail(barrister.getEmail(), authToken);
        if (userId.isEmpty()) {
            log.info("User id is empty");
            validationErrors.add(validationMessageForInvalidEmail(currentRepresentativeIndex, sizeOfRepresentatives));
        } else {
            if (assignCaseAccessService.isLegalCounselRepresentingOpposingLitigant(userId.get(), caseId, opposingRoles)) {
                validationErrors.add(validationMessageForAlreadyRepresentingOpposition(currentRepresentativeIndex, sizeOfRepresentatives));
            }
        }
        log.info("Barrister id is: {}", userId);
        return validationErrors;
    }

    private String validationMessageForInvalidEmail(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return String.format(""" 
                Email address for Barrister%s is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register""",
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String validationMessageForAlreadyRepresentingOpposition(long currentRepresentativeIdx,
                                                                     int sizeOfRepresentatives) {
        return String.format("Barrister%s is already representing another party on this case",
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String addNumericIfMultipleElements(long currentRepresentativeIndex, int sizeOfRepresentatives) {
        return sizeOfRepresentatives > 1 ? String.format(" %d", currentRepresentativeIndex + 1) : "";
    }
}
