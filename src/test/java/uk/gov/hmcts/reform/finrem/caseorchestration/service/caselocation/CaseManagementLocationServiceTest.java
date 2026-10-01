package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseManagementLocationServiceTest {

    private CaseManagementLocationService service;

    @BeforeEach
    void setUp() {
        service = new CaseManagementLocationService(Map.of(
            "fr_bristollist_3", CourtRefData.builder()
                .epimmsId("438850")
                .regionId("6")
                .courtName("Swindon Combined Court")
                .build()
        ));
    }

    @Test
    void shouldReturnCaseLocationWhenCourtExists() {
        FinremCaseData caseData = createCaseData(BristolCourt.SWINDON_COMBINED_COURT);

        CaseLocation location = service.getCaseLocation(caseData);

        assertThat(location).isNotNull();
        assertThat(location.getBaseLocation()).isEqualTo("438850");
        assertThat(location.getRegion()).isEqualTo("6");
    }

    @Test
    void shouldReturnNullWhenCourtListDoesNotExist() {
        FinremCaseData caseData = createCaseDataWithNoCourtSelection(Region.SOUTHWEST);

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtListValueIsUnknown() {
        FinremCaseData caseData = createCaseDataWithNoCourtSelection(Region.NORTHWEST);

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtListValueIsBlank() {
        FinremCaseData caseData = createCaseDataWithNoCourtSelection(Region.SOUTHWEST);

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCaseDataIsEmpty() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .workingHearing(WorkingHearing.builder()
                    .hearingCourtSelection(Court.builder().region(Region.SOUTHWEST).build())
                    .build())
                .build())
            .build();

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldUseSelectedWorkingHearingCourt() {
        FinremCaseData caseData = createCaseData(BristolCourt.SWINDON_COMBINED_COURT);

        CaseLocation location = service.getCaseLocation(caseData);

        assertThat(location).isNotNull();
        assertThat(location.getBaseLocation()).isEqualTo("438850");
        assertThat(location.getRegion()).isEqualTo("6");
    }

    private FinremCaseData createCaseData(BristolCourt bristolCourt) {
        Court court = Court.builder()
            .region(Region.SOUTHWEST)
            .southWestList(RegionSouthWestFrc.BRISTOL)
            .courtListWrapper(DefaultCourtListWrapper.builder()
                .bristolCourtList(bristolCourt)
                .build())
            .build();

        return buildCaseDataWithCourt(court);
    }

    private FinremCaseData createCaseDataWithNoCourtSelection(Region region) {
        Court court = Court.builder()
            .region(region)
            .build();

        return buildCaseDataWithCourt(court);
    }

    private FinremCaseData buildCaseDataWithCourt(Court court) {
        return FinremCaseData.builder()
            .ccdCaseId("1234")
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .workingHearing(WorkingHearing.builder()
                    .hearingCourtSelection(court)
                    .build())
                .build())
            .build();
    }
}
