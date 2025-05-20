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
    private static final String CCD_CASE_NUMBER = "ccdCaseNumber";
    private static final String APPLICANT_NAME = "applicantName";
    private static final String RESPONDENT_NAME = "respondentName";
    private static final String LETTER_DATE = "letterDate";
    private static final String HEARING_TYPE = "hearingType";
    private static final String HEARING_DATE = "hearingDate";
    private static final String HEARING_TIME = "hearingTime";
    private static final String HEARING_TIME_ESTIMATE = "hearingTimeEstimate";
    private static final String COURT_DETAILS = "courtDetails";
    private static final String HEARING_VENUE = "hearingVenue";
    private static final String ATTENDANCE = "attendance";
    private static final String ADDITIONAL_HEARING_INFORMATION = "additionalHearingInformation";

    /**
     * Generates a hearing notice document for the given hearing and case details.
     *
     * @param hearing the hearing information to include in the notice
     * @param finremCaseDetails the case details containing case data
     * @param authorisationToken the authorisation token for document generation
     * @return the generated hearing notice as a {@link CaseDocument}
     */
    public CaseDocument generateHearingNotice(Hearing hearing,
                                              FinremCaseDetails finremCaseDetails,
                                              String authorisationToken) {

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Map<String, Object> documentDataMap = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails).getData();

        documentDataMap.put(CCD_CASE_NUMBER, finremCaseDetails.getId().toString());
        documentDataMap.put(APPLICANT_NAME, finremCaseData.getFullApplicantName());
        documentDataMap.put(RESPONDENT_NAME, finremCaseData.getFullRespondentNameContested());
        documentDataMap.put(LETTER_DATE, LocalDate.now().toString());
        documentDataMap.put(HEARING_TYPE, hearing.getHearingType());
        documentDataMap.put(HEARING_DATE, hearing.getHearingDate().toString());
        documentDataMap.put(HEARING_TIME, hearing.getHearingTime());
        documentDataMap.put(HEARING_TIME_ESTIMATE, hearing.getHearingTimeEstimate());
        documentDataMap.put(COURT_DETAILS, buildHearingFrcCourtDetails(finremCaseData));
        documentDataMap.put(HEARING_VENUE, courtDetailsConfiguration.getCourts().get(finremCaseData.getSelectedHearingCourt()).getCourtAddress());
        documentDataMap.put(
            ATTENDANCE,
            hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : ""
        );
        documentDataMap.put(
            ADDITIONAL_HEARING_INFORMATION,
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
