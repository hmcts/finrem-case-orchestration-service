package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarristerRepresentationChecker {

    private final ObjectMapper objectMapper;

    private final BiPredicate<String, RepresentationUpdate> hasUserBeenBarrister = (email, update) ->
        (getChangedRepresentativeEmail(update.getAdded()).equals(email)
            || getChangedRepresentativeEmail(update.getRemoved()).equals(email))
        && update.getVia().equals(MANAGE_BARRISTERS);

    public boolean hasUserBeenBarristerOnCase(Map<String, Object>  caseData, UserDetails solicitor) {
        List<Element<RepresentationUpdate>> representationUpdateHistory = objectMapper.registerModule(new JavaTimeModule())
            .convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});

        return representationUpdateHistory.stream()
            .map(Element::getValue)
            .anyMatch(representationUpdate -> hasUserBeenBarrister.test(solicitor.getEmail(), representationUpdate));
    }

    private String getChangedRepresentativeEmail(ChangedRepresentative changedRepresentative) {
        return Optional.ofNullable(changedRepresentative).map(ChangedRepresentative::getEmail).orElse(StringUtils.EMPTY);
    }
}
