package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimUploadAdditionalDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ALL_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterimHearingItemMapper {

    private final InterimHearingHelper interimHearingHelper;

    public InterimHearingItem loadInterimHearingData(Map<String, Object> caseData) {
        return InterimHearingItem.builder()
            .interimHearingType((String) caseData.get(INTERIM_HEARING_TYPE))
            .interimHearingDate((String) caseData.get(INTERIM_HEARING_DATE))
            .interimHearingTime((String) caseData.get(INTERIM_HEARING_TIME))
            .interimHearingTimeEstimate((String) caseData.get(INTERIM_HEARING_TIME_ESTIMATE))
            .interimAdditionalInformationAboutHearing((String) caseData.get(INTERIM_HEARING_ADDITIONAL_INFO))
            .interimPromptForAnyDocument((String) caseData.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT))
            .interimRegionList((String) caseData.get(INTERIM_HEARING_REGION_LIST))
            .interimCfcCourtList((String) caseData.get(INTERIM_HEARING_CFC_COURT_LIST))
            .interimWalesFRCList((String) caseData.get(INTERIM_HEARING_WALES_FRC_COURT_LIST))
            .interimLondonFRCList((String) caseData.get(INTERIM_HEARING_LONDON_FRC_COURT_LIST))
            .interimDevonCourtList((String) caseData.get(INTERIM_HEARING_DEVON_COURT_LIST))
            .interimDorsetCourtList((String) caseData.get(INTERIM_HEARING_DORSET_COURT_LIST))
            .interimHumberCourtList((String) caseData.get(INTERIM_HEARING_HUMBER_COURT_LIST))
            .interimMidlandsFRCList((String) caseData.get(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST))
            .interimBristolCourtList((String) caseData.get(INTERIM_HEARING_BRISTOL_COURT_LIST))
            .interimNewportCourtList((String) caseData.get(INTERIM_HEARING_NEWPORT_COURT_LIST))
            .interimNorthEastFRCList((String) caseData.get(INTERIM_HEARING_NORTHEAST_COURT_LIST))
            .interimNorthWestFRCList((String) caseData.get(INTERIM_HEARING_NORTHWEST_COURT_LIST))
            .interimSouthEastFRCList((String) caseData.get(INTERIM_HEARING_SOUTHEAST_COURT_LIST))
            .interimSouthWestFRCList((String) caseData.get(INTERIM_HEARING_SOUTHWEST_COURT_LIST))
            .interimSwanseaCourtList((String) caseData.get(INTERIM_HEARING_SWANSEA_COURT_LIST))
            .interimLiverpoolCourtList((String) caseData.get(INTERIM_HEARING_LIVERPOOL_COURT_LIST))
            .interimBirminghamCourtList((String) caseData.get(INTERIM_HEARING_BIRMINGHAM_COURT_LIST))
            .interimCleavelandCourtList((String) caseData.get(INTERIM_HEARING_CLEAVELAND_COURT_LIST))
            .interimKentSurreyCourtList((String) caseData.get(INTERIM_HEARING_KENT_SURREY_COURT_LIST))
            .interimLancashireCourtList((String) caseData.get(INTERIM_HEARING_LANCASHIRE_COURT_LIST))
            .interimManchesterCourtList((String) caseData.get(INTERIM_HEARING_MANCHESTER_COURT_LIST))
            .interimNorthWalesCourtList((String) caseData.get(INTERIM_HEARING_NORTHWALES_COURT_LIST))
            .interimNottinghamCourtList((String) caseData.get(INTERIM_HEARING_NOTTINGHAM_COURT_LIST))
            .interimNwyorkshireCourtList((String) caseData.get(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST))
            .interimBedfordshireCourtList((String) caseData.get(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST))
            .interimThamesvalleyCourtList((String) caseData.get(INTERIM_HEARING_THAMESVALLEY_COURT_LIST))
            .interimUploadAdditionalDocument(loadInterimUploadedDocument(caseData))
            .build();
    }

    private InterimUploadAdditionalDocument loadInterimUploadedDocument(Map<String, Object> caseData) {
        if (caseData.get(INTERIM_HEARING_UPLOADED_DOCUMENT) != null) {
            CaseDocument interimUploadAdditionalDocument  =
                interimHearingHelper.convertToCaseDocument(caseData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
            return InterimUploadAdditionalDocument.builder()
                .documentUrl(interimUploadAdditionalDocument.getDocumentUrl())
                .documentFilename(interimUploadAdditionalDocument.getDocumentFilename())
                .documentBinaryUrl(interimUploadAdditionalDocument.getDocumentBinaryUrl())
                .build();
        }
        return null;
    }

    public void loadBulkPrintDocuments(Map<String, Object> caseData) {
        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList = Optional.ofNullable(caseData.get(INTERIM_HEARING_ALL_DOCUMENT))
            .map(interimHearingHelper::convertToBulkPrintDocumentDataList).orElse(new ArrayList<>());

        bulkPrintDocumentsList.add(loadBulkPrintDocument(caseData));
        caseData.put(INTERIM_HEARING_ALL_DOCUMENT, bulkPrintDocumentsList);
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(Map<String, Object> caseData) {
        if (caseData.get(INTERIM_HEARING_DOCUMENT) != null) {
            CaseDocument bulkPrintDocument = interimHearingHelper.convertToCaseDocument(caseData.get(INTERIM_HEARING_DOCUMENT));
            return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
                .value(InterimHearingBulkPrintDocument.builder()
                    .caseDocument(CaseDocument.builder()
                        .documentUrl(bulkPrintDocument.getDocumentUrl())
                        .documentFilename(bulkPrintDocument.getDocumentFilename())
                        .documentBinaryUrl(bulkPrintDocument.getDocumentBinaryUrl())
                        .build()).build())
                .build();
        }
        return null;
    }

}
