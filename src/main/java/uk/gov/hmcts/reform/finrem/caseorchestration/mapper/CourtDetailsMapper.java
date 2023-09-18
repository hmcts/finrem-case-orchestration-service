package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;


@Slf4j
@Component
@RequiredArgsConstructor
public class CourtDetailsMapper {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("java:S3011")
    final BiPredicate<Field, CourtWrapper> fieldIsNotNullOrEmpty = (field, courtListWrapper) -> {
        try {
            field.setAccessible(true);
            return field.get(courtListWrapper) != null && !field.get(courtListWrapper).toString().trim().isEmpty();
        } catch (IllegalAccessException e) {
            log.error("Illegal Access occurred; message : {}, cause: {}", e.getMessage(), e.getCause());
            return false;
        }
    };

    public FrcCourtDetails getCourtDetails(CourtWrapper courtWrapper) {
        List<Field> initialisedCourtField = getInitialisedCourtField(courtWrapper);

        if (initialisedCourtField.size() != 1) {
            throw new IllegalStateException("There must be exactly one court selected in case data");
        }

        try {
            return convertToFrcCourtDetails(initialisedCourtField.get(0), courtWrapper);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not access court list object field");
        }
    }

    private List<Field> getInitialisedCourtField(CourtWrapper regionWrapper) {
        List<Field> allFields = Arrays.asList(regionWrapper.getClass().getDeclaredFields());
        return filterEmptyFields(allFields, regionWrapper);
    }

    private List<Field> filterEmptyFields(List<Field> allFields, CourtWrapper courtWrapper) {
        return allFields.stream()
            .filter(field -> fieldIsNotNullOrEmpty.test(field, courtWrapper))
            .toList();
    }

    @SuppressWarnings("java:S112")
    private FrcCourtDetails convertToFrcCourtDetails(Field initialisedCourtField,
                                                     CourtWrapper courtWrapper) throws Exception {
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        CourtList selectedCourtField = (CourtList) initialisedCourtField.get(courtWrapper);
        Object selectedCourtDetailsObject = courtDetailsMap.get(nullToEmpty(selectedCourtField.getSelectedCourtId()));

        if (selectedCourtDetailsObject == null) {
            return FrcCourtDetails.builder().courtName("").courtAddress("").phoneNumber("").email("").build();
        }

        return objectMapper.convertValue(selectedCourtDetailsObject, new TypeReference<>() {
        });
    }
}
