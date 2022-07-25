package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimRegionWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterimHearingItemMapper {

    private final DocumentHelper documentHelper;

    public InterimHearingItem loadInterimHearingData(FinremCaseData finremCaseData) {
        InterimWrapper interimData = finremCaseData.getInterimWrapper();
        InterimRegionWrapper regionData = finremCaseData.getRegionWrapper().getInterimRegionWrapper();
        return InterimHearingItem.builder()
            .interimHearingType(interimData.getInterimHearingType().getId())
            .interimHearingDate(String.valueOf(interimData.getInterimHearingDate()))
            .interimHearingTime(interimData.getInterimHearingTime())
            .interimHearingTimeEstimate(interimData.getInterimTimeEstimate())
            .interimAdditionalInformationAboutHearing(interimData.getInterimAdditionalInformationAboutHearing())
            .interimPromptForAnyDocument(interimData.getInterimPromptForAnyDocument().getYesOrNo())
            .interimRegionWrapper(documentHelper.deepCopy(regionData, InterimRegionWrapper.class))
            .interimUploadAdditionalDocument(interimData.getInterimUploadAdditionalDocument())
            .build();
    }

    public void loadBulkPrintDocuments(FinremCaseData caseData) {
        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            Optional.ofNullable(caseData.getInterimWrapper().getInterimHearingDocuments()).orElse(new ArrayList<>());


        bulkPrintDocumentsList.add(loadBulkPrintDocument(caseData));
        caseData.getInterimWrapper().setInterimHearingDocuments(bulkPrintDocumentsList);
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(FinremCaseData caseData) {
        if (caseData.getInterimWrapper().getInterimHearingDirectionsDocument() != null) {
            Document bulkPrintDocument = caseData.getInterimWrapper().getInterimHearingDirectionsDocument();
            return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
                .value(InterimHearingBulkPrintDocument.builder()
                    .bulkPrintDocument(Document.builder()
                        .url(bulkPrintDocument.getUrl())
                        .filename(bulkPrintDocument.getFilename())
                        .binaryUrl(bulkPrintDocument.getBinaryUrl())
                        .build()).build())
                .build();
        }
        return null;
    }

}
