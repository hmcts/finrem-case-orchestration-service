package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(MockitoExtension.class)
class HearingTest {

    @ParameterizedTest
    @MethodSource("provideHearings")
    void mapHearingToWorkingHearing(Hearing hearing, String hearingOrigin) {
        log.info("Testing mapping of Hearing to WorkingHearing with origin: {}", hearingOrigin);

        WorkingHearing workingHearing =
            Hearing.mapHearingToWorkingHearing(hearing, List.of(HearingType.values()));

        assertNotNull(workingHearing);
        assertEquals(hearing.getHearingDate(), workingHearing.getHearingDate());
        assertEquals(hearing.getHearingTimeEstimate(), workingHearing.getHearingTimeEstimate());
        assertEquals(hearing.getHearingTime(), workingHearing.getHearingTime());
        assertEquals(hearing.getHearingCourtSelection(), workingHearing.getHearingCourtSelection());
        assertEquals(hearing.getHearingMode(), workingHearing.getHearingMode());
        assertEquals(hearing.getAdditionalHearingInformation(), workingHearing.getAdditionalHearingInformation());
        assertEquals(hearing.getHearingNoticePrompt(), workingHearing.getHearingNoticePrompt());
        assertEquals(hearing.getAdditionalHearingDocPrompt(), workingHearing.getAdditionalHearingDocPrompt());
        assertEquals(hearing.getAdditionalHearingDocs(), workingHearing.getAdditionalHearingDocs());
        assertEquals(hearing.getPartiesOnCase().getFirst().getValue().getRole(),
            workingHearing.getPartiesOnCaseMultiSelectList().getListItems().getFirst().getCode());
    }

    private static Stream<Arguments> provideHearings() {
        return Stream.of(
            Arguments.of(Hearing.builder()
                .hearingType(HearingType.APPEAL_HEARING)
                .hearingDate(LocalDate.of(2023, 1, 1))
                .hearingTimeEstimate("2 hours")
                .hearingTime("10:00 AM")
                .hearingCourtSelection(Court.builder()
                    .region(Region.LONDON)
                    .londonList(RegionLondonFrc.LONDON)
                    .build())
                .hearingMode(HearingMode.IN_PERSON)
                .additionalHearingInformation("Info A")
                .hearingNoticePrompt(YesOrNo.YES)
                .additionalHearingDocPrompt(YesOrNo.NO)
                .additionalHearingDocs(List.of(DocumentCollectionItem.builder().value(CaseDocument
                    .builder()
                    .documentFilename("Test Document.pdf")
                    .documentUrl("http://example.com/document.pdf")
                    .documentBinaryUrl("http://example.com/document-binary.pdf")
                    .build()).build()))
                .partiesOnCase(List.of(PartyOnCaseCollectionItem
                    .builder()
                    .value(PartyOnCase
                        .builder()
                        .role("Some Party")
                        .label("Some Party Label")
                        .build())
                    .build()))
                .build(),
                "Non-migrated hearing"
            ),
            Arguments.of(Hearing.builder()
                    .hearingType(HearingType.DIR)
                    .hearingDate(LocalDate.of(2023, 1, 1))
                    .hearingTimeEstimate("2 hours")
                    .hearingTime("10:00 AM")
                    .wasMigrated(YesOrNo.YES)
                    .hearingCourtSelection(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .build())
                    .hearingMode(null)
                    .additionalHearingInformation("Info A")
                    .hearingNoticePrompt(null)
                    .additionalHearingDocPrompt(YesOrNo.NO)
                    .additionalHearingDocs(List.of(DocumentCollectionItem.builder().value(CaseDocument
                        .builder()
                        .documentFilename("Test Document.pdf")
                        .documentUrl("http://example.com/document.pdf")
                        .documentBinaryUrl("http://example.com/document-binary.pdf")
                        .build()).build()))
                    .partiesOnCase(List.of(PartyOnCaseCollectionItem
                        .builder()
                        .value(PartyOnCase
                            .builder()
                            .role("Some Party")
                            .label("Some Party Label")
                            .build())
                        .build()))
                    .build(),
                "Migrated hearing"
            )
        );
    }
}
