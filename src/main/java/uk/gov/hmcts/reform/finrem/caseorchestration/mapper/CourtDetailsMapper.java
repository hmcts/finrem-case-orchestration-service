package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;


@Slf4j
@Component
@RequiredArgsConstructor
public class CourtDetailsMapper {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("java:S3011")
    final BiPredicate<Field, CourtListWrapper> fieldIsNotNullOrEmpty = (field, courtListWrapper) -> {
        try {
            field.setAccessible(true);
            return field.get(courtListWrapper) != null && !field.get(courtListWrapper).toString().trim().isEmpty();
        } catch (IllegalAccessException e) {
            log.error("Illegal Access occurred; message : {}, cause: {}", e.getMessage(), e.getCause());
            return false;
        }
    };

    public CourtDetailsTemplateFields getCourtDetails(CourtListWrapper courtListWrapper) {
        List<Field> initialisedCourtField = getInitialisedCourtField(courtListWrapper);

        if (initialisedCourtField.size() != 1) {
            throw new IllegalStateException("There must be exactly one court selected in case data");
        }

        try {
            return convertToFrcCourtDetails(initialisedCourtField.get(0), courtListWrapper);
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
            .toList();
    }

    @SuppressWarnings("java:S112")
    private CourtDetailsTemplateFields convertToFrcCourtDetails(Field initialisedCourtField,
                                                                CourtListWrapper courtListWrapper) throws Exception {
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        CourtList selectedCourtField = (CourtList) initialisedCourtField.get(courtListWrapper);
        Object selectedCourtDetailsObject = courtDetailsMap.get(nullToEmpty(selectedCourtField.getSelectedCourtId()));

        if (selectedCourtDetailsObject == null) {
            return CourtDetailsTemplateFields.builder().courtName("").courtAddress("").phoneNumber("").email("").build();
        }

        return objectMapper.convertValue(selectedCourtDetailsObject, new TypeReference<>() {
        });
    }

    public AllocatedRegionWrapper getLatestAllocatedCourt(AllocatedRegionWrapper regionWrapperBefore,
                                                          AllocatedRegionWrapper regionWrapperActual,
                                                          Boolean isConsentedApplication) {

        AllocatedRegionWrapper regionWrapperProcessed =
            AllocatedRegionWrapper.builder().regionList(regionWrapperActual.getRegionList()).build();
        switch (regionWrapperActual.getRegionList()) {
            case WALES -> regionWrapperProcessed.setWalesFrcList(regionWrapperActual.getWalesFrcList());
            case LONDON -> regionWrapperProcessed.setLondonFrcList(regionWrapperActual.getLondonFrcList());
            case MIDLANDS -> regionWrapperProcessed.setMidlandsFrcList(regionWrapperActual.getMidlandsFrcList());
            case HIGHCOURT -> regionWrapperProcessed.setHighCourtFrcList(regionWrapperActual.getHighCourtFrcList());
            case NORTHEAST -> regionWrapperProcessed.setNorthEastFrcList(regionWrapperActual.getNorthEastFrcList());
            case NORTHWEST -> regionWrapperProcessed.setNorthWestFrcList(regionWrapperActual.getNorthWestFrcList());
            case SOUTHEAST -> regionWrapperProcessed.setSouthEastFrcList(regionWrapperActual.getSouthEastFrcList());
            case SOUTHWEST -> regionWrapperProcessed.setSouthWestFrcList(regionWrapperActual.getSouthWestFrcList());
            default -> throw new IllegalStateException("There must be exactly one region selected in case data");
        }

        List<Field> initialisedCourtFieldActual = getInitialisedCourtField(regionWrapperActual.getDefaultCourtListWrapper());
        List<String> actualCourtList =
            initialisedCourtFieldActual
                .stream().map(field -> {
                    try {
                        return ((CourtList) field.get(regionWrapperActual.getDefaultCourtListWrapper()))
                            .getSelectedCourtId();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        List<Field> initialisedCourtFieldBefore =
            getInitialisedCourtField(regionWrapperBefore.getDefaultCourtListWrapper());
        List<String> beforeCourtList =
            initialisedCourtFieldBefore
                .stream().map(field -> {
                    try {
                        return ((CourtList) field.get(regionWrapperBefore.getDefaultCourtListWrapper()))
                            .getSelectedCourtId();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        List<String> newlyAddedCourtList = new ArrayList<>(CollectionUtils.removeAll(actualCourtList, beforeCourtList));

        if (newlyAddedCourtList.size() == 0) {
            return regionWrapperActual;
        } else {
            regionWrapperProcessed.getDefaultCourtListWrapper()
                .setCourt(newlyAddedCourtList.get(0), isConsentedApplication);
        }


        return regionWrapperProcessed;
    }
}
