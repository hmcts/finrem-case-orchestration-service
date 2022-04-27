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

    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";

    public NotificationRequest createNotificationRequestForAppSolicitor(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? buildNotificationRequest(caseDetails, SOLICITOR_REFERENCE, CONSENTED_SOLICITOR_NAME, SOLICITOR_EMAIL, CONSENTED)
            : buildNotificationRequest(caseDetails, SOLICITOR_REFERENCE, CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_EMAIL, CONTESTED);
    }

    public NotificationRequest createNotificationRequestForRespSolicitor(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? buildNotificationRequest(caseDetails, RESP_SOLICITOR_REFERENCE, RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONSENTED)
            : buildNotificationRequest(caseDetails, RESP_SOLICITOR_REFERENCE, RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONTESTED);
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(CaseDetails caseDetails) {
        String solicitorReferenceKey, solicitorNameKey, solicitorEmailKey;

        if (isLastChangeOfRepresentationForRespondent(caseDetails)) {
            solicitorReferenceKey = RESP_SOLICITOR_REFERENCE;
            solicitorNameKey = RESP_SOLICITOR_NAME;
            solicitorEmailKey = RESP_SOLICITOR_EMAIL;
        } else {
            solicitorReferenceKey = SOLICITOR_REFERENCE;
            if (caseDataService.isConsentedApplication(caseDetails)) {
                solicitorNameKey = CONSENTED_SOLICITOR_NAME;
                solicitorEmailKey = SOLICITOR_EMAIL;
            } else {
                solicitorNameKey = CONTESTED_SOLICITOR_NAME;
                solicitorEmailKey = CONTESTED_SOLICITOR_EMAIL;
            }
        }

        return buildNotificationRequest(caseDetails, solicitorReferenceKey, solicitorNameKey, solicitorEmailKey, getCaseType(caseDetails));
    }

    private boolean isLastChangeOfRepresentationForRespondent(CaseDetails caseDetails) {
        return getLastChangeOfRepresentation(caseDetails).getParty().equals("Respondent");
    }

    private ChangeOfRepresentation getLastChangeOfRepresentation(CaseDetails caseDetails) {
        ChangeOfRepresentatives changeOfRepresentativesHistory = objectMapper
            .convertValue(caseDetails.getData().get(CHANGE_OF_REPRESENTATIVES), new TypeReference<>() {});
        ChangeOfRepresentation lastChangeOfRepresentation = Collections
            .max(changeOfRepresentativesHistory.getChangeOfRepresentation(), Comparator.comparing(c -> c.getValue().getDate()))
            .getValue();
        return lastChangeOfRepresentation;
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
                                                         String solicitorReference,
                                                         String solicitorName,
                                                         String solicitorEmail,
                                                         String caseType) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = caseDetails.getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setDivorceCaseNumber(Objects.toString(mapOfCaseData.get(DIVORCE_CASE_NUMBER)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(GENERAL_EMAIL_BODY)));
        notificationRequest.setCaseType(caseType);

        if (caseDataService.isContestedApplication(caseDetails)) {
            String selectedCourt = ContestedCourtHelper.getSelectedFrc(caseDetails);
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is {} for case ID: {}", selectedCourt, notificationRequest.getCaseReferenceNumber());
        }

        return notificationRequest;
    }
}
