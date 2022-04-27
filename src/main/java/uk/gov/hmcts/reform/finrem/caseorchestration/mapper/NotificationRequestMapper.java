package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationRequestMapper {

    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;

    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";


    public NotificationRequest getNotificationRequestForRespondentSolicitor(CaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getCaseDataKeysForRespondentSolicitor());
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? buildNotificationRequest(caseDetails, getConsentedCaseDataKeysForApplicantSolicitor())
            : buildNotificationRequest(caseDetails, getContestedCaseDataKeysForApplicantSolicitor());
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(CaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestChangeOfRepresentation(caseDetails)
            ? getNotificationRequestForRespondentSolicitor(caseDetails)
            : getNotificationRequestForApplicantSolicitor(caseDetails);
    }

    private SolicitorCaseDataKeysWrapper getContestedCaseDataKeysForApplicantSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(CONTESTED_SOLICITOR_EMAIL)
            .solicitorNameKey(CONTESTED_SOLICITOR_NAME)
            .solicitorReferenceKey(SOLICITOR_REFERENCE)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getConsentedCaseDataKeysForApplicantSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(SOLICITOR_EMAIL)
            .solicitorNameKey(CONSENTED_SOLICITOR_NAME)
            .solicitorReferenceKey(SOLICITOR_REFERENCE)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getCaseDataKeysForRespondentSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(RESP_SOLICITOR_EMAIL)
            .solicitorNameKey(RESP_SOLICITOR_NAME)
            .solicitorReferenceKey(RESP_SOLICITOR_REFERENCE)
            .build();
    }

    private boolean isRespondentSolicitorChangedOnLatestChangeOfRepresentation(CaseDetails caseDetails) {
        return getLastChangeOfRepresentation(caseDetails).getParty().equals(RESPONDENT);
    }

    private ChangeOfRepresentation getLastChangeOfRepresentation(CaseDetails caseDetails) {
        ChangeOfRepresentatives changeOfRepresentativesHistory = objectMapper
            .convertValue(caseDetails.getData().get(CHANGE_OF_REPRESENTATIVES), new TypeReference<>() {
            });
        return Collections
            .max(changeOfRepresentativesHistory.getChangeOfRepresentation(),
                Comparator.comparing(c -> c.getValue().getDate()))
            .getValue();
    }

    private String getCaseType(CaseDetails caseDetails) {
        String caseType;
        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseType = CONSENTED;
        } else {
            caseType = CONTESTED;
        }
        return caseType;
    }

    private NotificationRequest buildNotificationRequest(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = caseDetails.getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey())));
        notificationRequest.setDivorceCaseNumber(Objects.toString(mapOfCaseData.get(DIVORCE_CASE_NUMBER)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorNameKey())));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorEmailKey())));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(GENERAL_EMAIL_BODY)));
        notificationRequest.setCaseType(getCaseType(caseDetails));

        if (caseDataService.isContestedApplication(caseDetails)) {
            String selectedCourt = ContestedCourtHelper.getSelectedFrc(caseDetails);
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is {} for case ID: {}", selectedCourt, notificationRequest.getCaseReferenceNumber());
        }

        return notificationRequest;
    }
}
