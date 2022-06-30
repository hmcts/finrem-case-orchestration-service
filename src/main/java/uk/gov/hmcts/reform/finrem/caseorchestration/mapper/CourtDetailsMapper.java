package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@RequiredArgsConstructor
@Slf4j
public class CourtDetailsMapper {

    private final ObjectMapper objectMapper;

    final BiPredicate<Field, CourtListWrapper> fieldIsNotNullOrEmpty = (field, courtListWrapper) -> {
        try {
            field.setAccessible(true);
            return field.get(courtListWrapper) != null && !field.get(courtListWrapper).toString().trim().isEmpty();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    };

    public FrcCourtDetails getCourtDetails(CourtListWrapper courtListWrapper) {
        List<Field> initialisedCourtField = getInitialisedCourtField(courtListWrapper);

        if (initialisedCourtField.size() != 1) {
            throw new IllegalStateException("More than one court selected in case data");
        }

        try {
            return convertToFrcCourtDetails(initialisedCourtField, courtListWrapper);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not access court list object field");
        }
    }

    private List<Field> getInitialisedCourtField(CourtListWrapper regionWrapper) {
        List<Field> allFields = Arrays.asList(regionWrapper.getClass().getDeclaredFields());
        return filterEmptyFields(allFields, regionWrapper);
    }

    private List<Field> filterEmptyFields(List<Field> allFields, CourtListWrapper courtListWrapper) {
        return allFields.stream()
            .filter(field -> fieldIsNotNullOrEmpty.test(field, courtListWrapper))
            .collect(Collectors.toList());
    }

    private FrcCourtDetails convertToFrcCourtDetails(List<Field> initialisedCourtField,
                                                     CourtListWrapper courtListWrapper) throws Exception {
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);

        return objectMapper.convertValue(courtDetailsMap.get(
            nullToEmpty(initialisedCourtField.get(0).get(courtListWrapper))),
            new TypeReference<>() {});
    }
}
