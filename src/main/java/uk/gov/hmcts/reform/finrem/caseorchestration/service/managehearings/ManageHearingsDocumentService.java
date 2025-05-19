package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildHearingFrcCourtDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsDocumentService {

    private final CourtDetailsConfiguration courtDetailsConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final DocumentConfiguration documentConfiguration;

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    public CaseDocument generateHearingNotice(Hearing hearing,
                                              FinremCaseDetails finremCaseDetails,
                                              String authorisationToken) {

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Map<String, Object> documentDataMap = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails).getData();

        documentDataMap.put("ccdCaseNumber", finremCaseDetails.getId().toString());
        documentDataMap.put("applicantName", finremCaseData.getFullApplicantName());
        documentDataMap.put("respondentName", finremCaseData.getFullRespondentNameContested());
        documentDataMap.put("letterDate", LocalDate.now().toString());
        documentDataMap.put("hearingType", hearing.getHearingType());
        documentDataMap.put("hearingDate", hearing.getHearingDate().toString());
        documentDataMap.put("hearingTime", hearing.getHearingTime());
        documentDataMap.put("hearingTimeEstimate", hearing.getHearingTimeEstimate());
        documentDataMap.put("courtDetails", buildHearingFrcCourtDetails(finremCaseData));
        documentDataMap.put("hearingVenue", courtDetailsConfiguration.getCourts().get(finremCaseData.getSelectedHearingCourt()).getCourtAddress());
        documentDataMap.put(
                "attendance",
                hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : ""
        );
        documentDataMap.put(
                "additionalHearingInformation",
                hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : ""
        );

        HashMap<String, Object> caseDetailsMap = new HashMap<>(Map.of(
            CASE_DETAILS, Map.of(
                CASE_DATA, documentDataMap
            )
        ));

        CaseDocument hearingDoc = genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, caseDetailsMap,
                documentConfiguration.getManageHearingNoticeTemplate(),
                documentConfiguration.getManageHearingNoticeFileName(),
                finremCaseDetails.getId().toString());

        hearingDoc.setCategoryId(DocumentCategory.HEARING_DOCUMENTS.getDocumentCategoryId());

        return hearingDoc;
    }
}
