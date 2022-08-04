package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
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

    protected static final String EMPTY_STRING = "";
    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private final CaseDataService caseDataService;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final CourtDetailsMapper courtDetailsMapper;


    @Deprecated
    public NotificationRequest getNotificationRequestForRespondentSolicitor(CaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getCaseDataKeysForRespondentSolicitor());
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getCaseData()));
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails,
                                                                            InterimHearingCollection interimHearingData) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getCaseData()), interimHearingData);
    }

    @Deprecated
    public NotificationRequest getNotificationRequestForApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? buildNotificationRequest(caseDetails, getConsentedCaseDataKeysForApplicantSolicitor())
            : buildNotificationRequest(caseDetails, getContestedCaseDataKeysForApplicantSolicitor());
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getCaseData()));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails,
                                                                           InterimHearingCollection interimHearingData) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getCaseData()), interimHearingData);
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(FinremCaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate(caseDetails)
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

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(caseData.getAppSolicitorName())
            .solicitorReferenceKey(caseData.getContactDetailsWrapper().getSolicitorReference())
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(caseData.getRespondentSolicitorName())
            .solicitorReferenceKey(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
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

    private boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate(FinremCaseDetails caseDetails) {
        return getLastRepresentationUpdate(caseDetails).getParty().equalsIgnoreCase(RESPONDENT);
    }

    private RepresentationUpdate getLastRepresentationUpdate(FinremCaseDetails caseDetails) {
        List<RepresentationUpdateHistoryCollection> representationUpdates =
            caseDetails.getCaseData().getRepresentationUpdateHistory();

        return Collections.max(representationUpdates, Comparator.comparing(c -> c.getValue().getDate())).getValue();
    }

    @Deprecated
    private NotificationRequest buildNotificationRequest(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = getNotificationCoreData(caseDetails, solicitorCaseDataKeysWrapper);

        if (caseDataService.isContestedApplication(caseDetails)) {
            String selectedCourt = ContestedCourtHelper.getSelectedFrc(caseDetails);
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is {} for case ID: {}", selectedCourt, notificationRequest.getCaseReferenceNumber());
        }

        return notificationRequest;
    }

    private NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper,
                                                         InterimHearingCollection interimHearingData) {

        NotificationRequest notificationRequest = getNotificationCoreData(caseDetails, solicitorCaseDataKeysWrapper);
        CourtListWrapper courtInfo = interimHearingData.getValue().getInterimRegionWrapper().getCourtListWrapper();

        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtInfo);
        String selectedCourt = courtDetails.getCourtName();
        notificationRequest.setSelectedCourt(selectedCourt);

        log.info("selectedCourt is {} for case ID: {}", selectedCourt, notificationRequest.getCaseReferenceNumber());

        return notificationRequest;
    }

    private NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        FinremCaseData caseData = caseDetails.getCaseData();
        notificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey());
        notificationRequest.setDivorceCaseNumber(caseData.getDivorceCaseNumber());
        notificationRequest.setName(solicitorCaseDataKeysWrapper.getSolicitorNameKey());
        notificationRequest.setNotificationEmail(solicitorCaseDataKeysWrapper.getSolicitorEmailKey());
        notificationRequest.setCaseType(getCaseType(caseDetails));

        if (caseData.isContestedApplication()) {
            FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(caseData.getRegionWrapper().getDefaultCourtList());
            notificationRequest.setSelectedCourt(courtDetails.getCourtName());

            log.info("selectedCourt is {} for case ID: {}", courtDetails.getCourtName(),
                notificationRequest.getCaseReferenceNumber());
        }

        if (caseData.isConsentedApplication()) {
            setCaseOrderType(notificationRequest, caseData);
        }

        return notificationRequest;
    }

    private NotificationRequest getNotificationCoreData(FinremCaseDetails caseDetails,
                                                        SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        FinremCaseData caseData = caseDetails.getCaseData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey());
        notificationRequest.setDivorceCaseNumber(caseData.getDivorceCaseNumber());
        notificationRequest.setName(solicitorCaseDataKeysWrapper.getSolicitorNameKey());
        notificationRequest.setNotificationEmail(solicitorCaseDataKeysWrapper.getSolicitorEmailKey());
        notificationRequest.setGeneralEmailBody(caseData.getGeneralEmailBody());
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        if (caseData.isConsentedApplication()) {
            setCaseOrderType(notificationRequest, caseData);
        }

        return notificationRequest;
    }

    private NotificationRequest getNotificationCoreData(CaseDetails caseDetails, SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = caseDetails.getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey()),
            EMPTY_STRING));
        notificationRequest.setDivorceCaseNumber(Objects.toString(mapOfCaseData.get(DIVORCE_CASE_NUMBER)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorNameKey())));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorCaseDataKeysWrapper.getSolicitorEmailKey())));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(GENERAL_EMAIL_BODY)));
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        if (caseDataService.isConsentedApplication(caseDetails)) {
            if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(mapOfCaseData))) {
                notificationRequest.setCaseOrderType("variation");
                notificationRequest.setCamelCaseOrderType("Variation");
            } else {
                notificationRequest.setCaseOrderType("consent");
                notificationRequest.setCamelCaseOrderType("Consent");
            }
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }

        return notificationRequest;
    }

    private void setCaseOrderType(NotificationRequest notificationRequest, FinremCaseData caseData) {
        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseData))) {
            notificationRequest.setCaseOrderType("variation");
            notificationRequest.setCamelCaseOrderType("Variation");
        } else {
            notificationRequest.setCaseOrderType("consent");
            notificationRequest.setCamelCaseOrderType("Consent");
        }
        log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
            notificationRequest.getCaseReferenceNumber());
    }

    private String getCaseType(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseType().equals(CaseType.CONSENTED) ? CONSENTED : CONTESTED;
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
}
