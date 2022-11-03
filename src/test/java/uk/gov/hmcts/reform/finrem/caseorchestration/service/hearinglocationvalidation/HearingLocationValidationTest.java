package uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation.HearingLocationValidationService.HEARING_LOCATION_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class HearingLocationValidationTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdditionalHearingLocationValidationService additionalHearingLocationValidationService;

    @InjectMocks
    private DirectionDetailLocationValidationService directionDetailLocationValidationService;

    @InjectMocks
    private InterimHearingLocationValidationService interimHearingLocationValidationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(REGION, LONDON);
        caseDetails = CaseDetails.builder().id(1234567890L).data(caseData).build();
    }

    @Test
    public void givenLatestAdditionalHearingHasMismatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(HEARING_DIRECTION_DETAILS_COLLECTION,
            List.of(AdditionalHearingDirectionsCollectionElement.builder()
                .additionalHearingDirections(AdditionalHearingDirectionsCollection.builder()
                    .localCourt(Map.of(REGION_CT, SOUTHWEST))
                    .build())
                .build()));

        List<String> errors = additionalHearingLocationValidationService
            .validateHearingLocation(caseDetails, HEARING_DIRECTION_DETAILS_COLLECTION);

        assertThat(errors).containsExactly(HEARING_LOCATION_ERROR);
    }

    @Test
    public void givenLatestAdditionalHearingHasMatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(HEARING_DIRECTION_DETAILS_COLLECTION,
            List.of(AdditionalHearingDirectionsCollectionElement.builder()
                .additionalHearingDirections(AdditionalHearingDirectionsCollection.builder()
                    .localCourt(Map.of(REGION_CT, LONDON))
                    .build())
                .build()));

        List<String> errors = additionalHearingLocationValidationService
            .validateHearingLocation(caseDetails, HEARING_DIRECTION_DETAILS_COLLECTION);

        assertThat(errors).isEmpty();
    }

    @Test
    public void givenInterimHearingHasMismatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(INTERIM_HEARING_COLLECTION, List.of(
            InterimHearingData.builder()
                .value(InterimHearingItem.builder()
                    .interimRegionList(SOUTHWEST)
                    .build())
                .build()
        ));

        List<String> errors = interimHearingLocationValidationService
            .validateHearingLocation(caseDetails, INTERIM_HEARING_COLLECTION);

        assertThat(errors).containsExactly(HEARING_LOCATION_ERROR);
    }

    @Test
    public void givenInterimHearingHasMatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(INTERIM_HEARING_COLLECTION, List.of(
            InterimHearingData.builder()
                .value(InterimHearingItem.builder()
                    .interimRegionList(LONDON)
                    .build())
                .build()
        ));

        List<String> errors = interimHearingLocationValidationService
            .validateHearingLocation(caseDetails, INTERIM_HEARING_COLLECTION);

        assertThat(errors).isEmpty();
    }

    @Test
    public void givenDirectionDetailHasMismatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(DIRECTION_DETAILS_COLLECTION_CT, List.of(
            DirectionDetailsCollectionData.builder()
                .directionDetailsCollection(DirectionDetailsCollection.builder()
                    .localCourt(Map.of(REGION_CT, SOUTHWEST))
                    .build())
                .build()
        ));

        List<String> errors = directionDetailLocationValidationService
            .validateHearingLocation(caseDetails, DIRECTION_DETAILS_COLLECTION_CT);

        assertThat(errors).containsExactly(HEARING_LOCATION_ERROR);
    }

    @Test
    public void givenDirectionDetailHasMatchingRegion_whenValidateHearingLocation_thenReturnError() {
        caseDetails.getData().put(DIRECTION_DETAILS_COLLECTION_CT, List.of(
            DirectionDetailsCollectionData.builder()
                .directionDetailsCollection(DirectionDetailsCollection.builder()
                    .localCourt(Map.of(REGION_CT, LONDON))
                    .build())
                .build()
        ));

        List<String> errors = directionDetailLocationValidationService
            .validateHearingLocation(caseDetails, DIRECTION_DETAILS_COLLECTION_CT);

        assertThat(errors).isEmpty();
    }

    @Test
    public void givenCollectionIsNullOrEmpty_whenValidateHearingLocation_thenHandleGracefully() {
        List<String> errors = directionDetailLocationValidationService
            .validateHearingLocation(caseDetails, DIRECTION_DETAILS_COLLECTION_CT);

        assertThat(errors).isEmpty();
    }
}
