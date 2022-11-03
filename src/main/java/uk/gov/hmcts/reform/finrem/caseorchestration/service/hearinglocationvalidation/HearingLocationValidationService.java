package uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingLocationCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

@Service
@Slf4j
public abstract class HearingLocationValidationService<T extends HearingLocationCollection> {

    private final ObjectMapper objectMapper;
    private final Class<T> collectionType;
    public static final String HEARING_LOCATION_ERROR = "Hearing Location region must match the administrative region.\n"
        + "If you wish to change the administrative region please run the Update FRC Information event";
    public static final String IMPOSSIBLE_REGION = "IMPOSSIBLAW";

    private final BiPredicate<T, Map<String, Object>> isNotMatchingAdministrativeRegion = (element, caseData) ->
        !Objects.toString(caseData.get(REGION), IMPOSSIBLE_REGION).equalsIgnoreCase(element.getRegion());

    public HearingLocationValidationService(ObjectMapper objectMapper, Class<T> collectionType) {
        this.objectMapper = objectMapper;
        this.collectionType = collectionType;
    }

    public List<String> validateHearingLocation(CaseDetails caseDetails, String collectionKey) {
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors = newArrayList();
        List<T> hearingCollectionData = getHearingLocationCollection(caseData, collectionKey);

        Optional.ofNullable(Iterables.getLast(hearingCollectionData, null))
            .filter(element -> isNotMatchingAdministrativeRegion.test(element, caseData))
            .ifPresent(element -> errors.add(HEARING_LOCATION_ERROR));

        return errors;
    }

    private List<T> getHearingLocationCollection(Map<String, Object> caseData, String collectionKey) {
        List<T> documentList = objectMapper.convertValue(caseData.get(collectionKey),
            TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, collectionType));

        return Optional.ofNullable(documentList).orElse(Collections.emptyList());
    }
}
