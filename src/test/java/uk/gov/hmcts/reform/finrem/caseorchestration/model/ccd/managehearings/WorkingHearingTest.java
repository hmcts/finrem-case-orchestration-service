package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkingHearingTest {

    @ParameterizedTest
    @MethodSource
    void testWithHearingTypes(HearingType... hearingTypes) {
        String[] expectedCodes = Arrays.stream(hearingTypes).map(HearingType::name).toArray(String[]::new);
        String[] expectedLabels = Arrays.stream(hearingTypes).map(HearingType::getId).toArray(String[]::new);

        WorkingHearing workingHearing = WorkingHearing.builder()
            .withHearingTypes(hearingTypes)
            .build();

        List<DynamicListElement> listItems = workingHearing.getHearingTypeDynamicList().getListItems();
        assertThat(listItems)
            .extracting(DynamicListElement::getCode)
            .containsExactly(expectedCodes);
        assertThat(listItems)
            .extracting(DynamicListElement::getLabel)
            .containsExactly(expectedLabels);
    }

    private static Stream<Arguments> testWithHearingTypes() {
        return Stream.of(
            Arguments.of((Object) new HearingType[]{HearingType.FDA}),
            Arguments.of((Object) new HearingType[]{HearingType.FDA, HearingType.DIR, HearingType.MPS}),
            Arguments.of((Object) HearingType.values())
        );
    }

    @Test
    void testWithHearingTypeSelected() {
        WorkingHearing workingHearing = WorkingHearing.builder()
            .withHearingTypeSelected(HearingType.FDA)
            .build();

        DynamicListElement value = workingHearing.getHearingTypeDynamicList().getValue();
        assertThat(value.getCode()).isEqualTo(HearingType.FDA.name());
        assertThat(value.getLabel()).isEqualTo(HearingType.FDA.getId());
    }

    @Test
    void givenHearingTypesAndSelectedHearing_whenBuild_thenHearingTypeListPopulated() {
        WorkingHearing workingHearing = WorkingHearing.builder()
            .withHearingTypes(HearingType.FDA, HearingType.DIR, HearingType.MPS)
            .withHearingTypeSelected(HearingType.DIR)
            .build();

        DynamicList hearingTypeDynamicList = workingHearing.getHearingTypeDynamicList();
        assertThat(hearingTypeDynamicList.getListItems()).hasSize(3);
        assertThat(hearingTypeDynamicList.getValue().getCode()).isEqualTo(HearingType.DIR.name());
    }

    @ParameterizedTest
    @MethodSource
    void testWithPartiesOnCase(List<PartyOnCase> partiesOnCase) {
        List<PartyOnCaseCollectionItem> partyOnCaseCollectionItems = partiesOnCase.stream()
            .map(PartyOnCaseCollectionItem::new)
            .toList();

        String[] expectedCodes = partyOnCaseCollectionItems.stream()
            .map(PartyOnCaseCollectionItem::getValue)
            .map(PartyOnCase::getRole)
            .toArray(String[]::new);
        String[] expectedLabels = partyOnCaseCollectionItems.stream()
            .map(PartyOnCaseCollectionItem::getValue)
            .map(PartyOnCase::getLabel)
            .toArray(String[]::new);

        WorkingHearing workingHearing = WorkingHearing.builder()
            .withPartiesOnCase(partyOnCaseCollectionItems)
            .build();

        List<DynamicMultiSelectListElement> listItems = workingHearing.getPartiesOnCaseMultiSelectList().getListItems();
        assertThat(listItems)
            .extracting(DynamicMultiSelectListElement::getCode)
            .containsExactly(expectedCodes);
        assertThat(listItems)
            .extracting(DynamicMultiSelectListElement::getLabel)
            .containsExactly(expectedLabels);
    }

    private static Stream<Arguments> testWithPartiesOnCase() {
        return Stream.of(
            Arguments.of(List.of(
                partyOnCase("role1", "label1"))),
            Arguments.of(List.of(
                partyOnCase("role1", "label1"),
                partyOnCase("role2", "label2"),
                partyOnCase("role3", "label3")))
        );
    }

    private static PartyOnCase partyOnCase(String role, String label) {
        return PartyOnCase.builder()
            .role(role)
            .label(label)
            .build();
    }

    @ParameterizedTest
    @MethodSource
    void testFrom(Hearing hearing) {
        WorkingHearing workingHearing = WorkingHearing.from(hearing);

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

    private static Stream<Arguments> testFrom() {
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
                        .documentUrl("https://example.com/document.pdf")
                        .documentBinaryUrl("https://example.com/document-binary.pdf")
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
                        .documentUrl("https://example.com/document.pdf")
                        .documentBinaryUrl("https://example.com/document-binary.pdf")
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
