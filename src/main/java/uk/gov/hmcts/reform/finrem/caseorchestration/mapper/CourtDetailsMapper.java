package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtDetailsWrapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@RequiredArgsConstructor
@Slf4j
public class CourtDetailsMapper {
    private static final String COURT_LIST = "CourtList";

    private final ObjectMapper objectMapper;

    final Predicate<Field> isCourtList = field -> field.getName().contains(COURT_LIST);
    final BiPredicate<Field, CourtDetailsWrapper> fieldIsNotNullOrEmpty = (field, regionWrapper) -> {
        try {
            return field.get(regionWrapper) != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    };

    public Map<String, Object> getCourtDetails(CourtDetailsWrapper regionWrapper) {
        List<Field> initialisedCourtField = getInitialisedCourtField(regionWrapper);

        if (initialisedCourtField.size() != 1) {
            throw new IllegalStateException("More than one court selected in case data");
        }

        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            return (Map<String, Object>) courtDetailsMap.get(initialisedCourtField.get(0).get(regionWrapper));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private List<Field> getInitialisedCourtField(CourtDetailsWrapper regionWrapper) {
        List<Field> allFields = Arrays.asList(regionWrapper.getClass().getDeclaredFields());
        return filterNullAndAndNonCourtFields(allFields, regionWrapper);
    }

    private List<Field> filterNullAndAndNonCourtFields(List<Field> allFields, CourtDetailsWrapper regionWrapper) {
        allFields.forEach(field -> field.setAccessible(true));
        return allFields.stream()
            .filter(isCourtList)
            .filter(field -> fieldIsNotNullOrEmpty.test(field, regionWrapper))
            .collect(Collectors.toList());
    }
}
