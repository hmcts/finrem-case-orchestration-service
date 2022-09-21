package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarristerEmailValidationService {

    private final PrdOrganisationService organisationService;

    public List<String> validateBarristerEmails(List<BarristerData> barristers, String authToken) {
        return Streams.mapWithIndex(
            barristers.stream(),
            (barristerData, index) -> performBasicValidation(barristerData.getBarrister(), index, barristers.size(), authToken))
            .flatMap(Collection::stream).toList();
    }

    private List<String> performBasicValidation(Barrister barrister, long currentRepresentativeIndex,
                                                int sizeOfRepresentatives, String authToken) {
        List<String> validationErrors = newArrayList();

        Optional<String> userId = organisationService.findUserByEmail(barrister.getEmail(), authToken);
        if (userId.isEmpty()) {
            validationErrors.add(validationMessageForInvalidEmail(currentRepresentativeIndex, sizeOfRepresentatives));
        }

        return validationErrors;
    }

    private String validationMessageForInvalidEmail(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return String.format(""" 
                Email address for Barrister%s is not registered with myHMCTS.
                They can register at https://manage-org.platform.hmcts.net/register-org/register""",
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String addNumericIfMultipleElements(long currentRepresentativeIndex, int sizeOfRepresentatives) {
        return sizeOfRepresentatives > 1 ? String.format(" %d", currentRepresentativeIndex + 1) : "";
    }
}
