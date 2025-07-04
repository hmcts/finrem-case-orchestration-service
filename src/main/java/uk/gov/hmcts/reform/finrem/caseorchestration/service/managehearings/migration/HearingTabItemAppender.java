package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.addItemToList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

@Component
@Slf4j
public class HearingTabItemAppender {

    private final HearingTabDataMapper hearingTabDataMapper;

    public HearingTabItemAppender(HearingTabDataMapper hearingTabDataMapper) {
        this.hearingTabDataMapper = hearingTabDataMapper;
    }

    public void appendToHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearingTabItems,
            caseData.getManageHearingsWrapper()::setHearingTabItems, item);
        /* Requires DFR-3831 and DFR-3587
        addItemToList(caseData.getManageHearingsWrapper()::getApplicantHHearingTabItems,
            caseData.getManageHearingsWrapper()::setApplicantHHearingTabItems, item);
        addItemToList(caseData.getManageHearingsWrapper()::getRespondentHHearingTabItems,
                caseData.getManageHearingsWrapper()::setRespondentHHearingTabItems, item);
         */
    }

    public void appendToHearings(FinremCaseData caseData, ManageHearingsCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearings,
            caseData.getManageHearingsWrapper()::setHearings, item);
    }

    public Hearing toHearing(ListForHearingWrapper listForHearingWrapper) {
        // Type of Hearing
        HearingTypeDirection hearingType = listForHearingWrapper.getHearingType();
        // Hearing Date
        LocalDate hearingDate = listForHearingWrapper.getHearingDate();
        // Hearing Time
        String hearingTime = listForHearingWrapper.getHearingTime();
        // Time Estimate
        String timeEstimate = listForHearingWrapper.getTimeEstimate();
        // Additional information about the hearing
        String additionalInformationAboutHearing = listForHearingWrapper.getAdditionalInformationAboutHearing();
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        HearingRegionWrapper hearingRegionWrapper = listForHearingWrapper.getHearingRegionWrapper();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(HearingType.valueOf(hearingType.name()))
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingRegionWrapper.toCourt())
            //.hearingMode(null) // Ignore it because existing List for Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toAdditionalHearingDocs(listForHearingWrapper))
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    public Hearing toHearing(InterimHearingItem interimHearingItem) {
        // Type of Hearing
        InterimTypeOfHearing hearingType = interimHearingItem.getInterimHearingType();
        // Hearing Date
        LocalDate hearingDate = interimHearingItem.getInterimHearingDate();
        // Hearing Time
        String hearingTime = interimHearingItem.getInterimHearingTime();
        // Time Estimate
        String timeEstimate = interimHearingItem.getInterimHearingTimeEstimate();
        // Additional information about the hearing
        String additionalInformationAboutHearing = interimHearingItem.getInterimAdditionalInformationAboutHearing();
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = interimHearingItem.toCourt();
        DocumentCollectionItem additionalDocument = interimHearingItem.getInterimUploadAdditionalDocument() == null ? null :
            DocumentCollectionItem.builder().value(interimHearingItem.getInterimUploadAdditionalDocument()).build();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(HearingType.valueOf(hearingType.name()))
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing List for Interim Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toSingletonListOrNull(additionalDocument))
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    public HearingTabItem toHearingTabItem(ListForHearingWrapper listForHearingWrapper) {
        // Type of Hearing
        HearingTypeDirection hearingType = listForHearingWrapper.getHearingType();
        // Hearing Date
        LocalDate hearingDate = listForHearingWrapper.getHearingDate();
        // Hearing Time
        String hearingTime = listForHearingWrapper.getHearingTime();
        // Time Estimate
        String timeEstimate = listForHearingWrapper.getTimeEstimate();
        // Additional information about the hearing
        String additionalInformationAboutHearing = listForHearingWrapper.getAdditionalInformationAboutHearing();
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        HearingRegionWrapper hearingRegionWrapper = listForHearingWrapper.getHearingRegionWrapper();

        return HearingTabItem.builder()
            .tabHearingType(hearingType.getId())
            .tabCourtSelection(hearingTabDataMapper.getCourtName(hearingRegionWrapper.toCourt()))
            .tabDateTime(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime))
            .tabTimeEstimate(timeEstimate)
            .tabConfidentialParties("Unknown")
            .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(additionalInformationAboutHearing))
            .tabHearingMigratedDate(LocalDateTime.now())
            .tabHearingDocuments(toAdditionalHearingDocs(listForHearingWrapper))
            .build();
    }

    public HearingTabItem toHearingTabItem(InterimHearingItem interimHearingItem) {
        return HearingTabItem.builder()
            .tabHearingType(interimHearingItem.getInterimHearingType().getId())
            .tabCourtSelection(hearingTabDataMapper.getCourtName(interimHearingItem.toCourt()))
            .tabDateTime(hearingTabDataMapper.getFormattedDateTime(
                interimHearingItem.getInterimHearingDate(), interimHearingItem.getInterimHearingTime()))
            .tabTimeEstimate(interimHearingItem.getInterimHearingTimeEstimate())
            .tabConfidentialParties("Unknown")  // Cannot migrate "Who has received this notice"
            .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(
                interimHearingItem.getInterimAdditionalInformationAboutHearing()))
            .tabHearingMigratedDate(LocalDateTime.now())
            .tabHearingDocuments(toAdditionalHearingDocs(interimHearingItem))
            .build();
    }

    private List<DocumentCollectionItem> toAdditionalHearingDocs(ListForHearingWrapper listForHearingWrapper) {
        CaseDocument doc = listForHearingWrapper.getAdditionalListOfHearingDocuments();
        return doc == null ? null : List.of(DocumentCollectionItem.builder().value(doc).build());
    }

    private List<DocumentCollectionItem> toAdditionalHearingDocs(InterimHearingItem interimHearingItem) {
        CaseDocument doc = interimHearingItem.getInterimUploadAdditionalDocument();
        return doc == null ? null : List.of(DocumentCollectionItem.builder().value(doc).build());
    }

}
