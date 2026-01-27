package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
public class FinremNotificationRequestMapper extends AbstractNotificationRequestMapper {

    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private static final String EMPTY_STRING = "";

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public FinremNotificationRequestMapper(NotificationRequestBuilderFactory notificationRequestBuilderFactory,
                                           ConsentedApplicationHelper consentedApplicationHelper) {
        super(notificationRequestBuilderFactory);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails, boolean isNotDigital) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getData(), isNotDigital));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails, boolean isNotDigital) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getData(), isNotDigital));
    }

    public NotificationRequest getNotificationRequestForIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                            SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        return buildNotificationRequest(caseDetails, caseDataKeysWrapper);
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(FinremCaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate(caseDetails)
            ? getNotificationRequestForRespondentSolicitor(caseDetails)
            : getNotificationRequestForApplicantSolicitor(caseDetails);
    }

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getAppSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference()))
            .build();
    }

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData, boolean isNotDigital) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getAppSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference()))
            .solicitorIsNotDigitalKey(isNotDigital)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getRespondentSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference()))
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData, boolean isNotDigital) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getRespondentSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference()))
            .solicitorIsNotDigitalKey(isNotDigital)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getIntervenerSolicitorCaseData(FinremCaseData caseData, IntervenerType intervenerType) {
        IntervenerDetails intervenerDetails = caseData.getIntervenerById(intervenerType.getIntervenerId());

        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(intervenerDetails.getIntervenerSolEmail())
            .solicitorNameKey(nullToEmpty(intervenerDetails.getIntervenerSolName()))
            .solicitorReferenceKey(nullToEmpty(intervenerDetails.getIntervenerSolicitorReference()))
            .build();
    }

    private boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate(FinremCaseDetails caseDetails) {
        return getLastRepresentationUpdate(caseDetails).getParty().equalsIgnoreCase(RESPONDENT);
    }

    private RepresentationUpdate getLastRepresentationUpdate(FinremCaseDetails caseDetails) {
        List<RepresentationUpdateHistoryCollection> representationUpdates =
            caseDetails.getData().getRepresentationUpdateHistory();

        return Collections.max(representationUpdates, Comparator.comparing(c -> c.getValue().getDate())).getValue();
    }

    private NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        final FinremCaseData caseData = caseDetails.getData();

        // TODO Consider using James' NotificationRequestBuilder
        NotificationRequest notificationRequest = NotificationRequest.builder().build();
        notificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        notificationRequest.setDivorceCaseNumber(Objects.toString(caseData.getDivorceCaseNumber(), EMPTY_STRING));
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        notificationRequest.setGeneralApplicationRejectionReason(
            Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationRejectReason(), EMPTY_STRING));
        notificationRequest.setGeneralEmailBody(Objects.toString(caseData.getGeneralEmailWrapper().getGeneralEmailBody(), EMPTY_STRING));
        notificationRequest.setApplicantName(caseData.getFullApplicantName());
        if (caseData.isConsentedApplication()) {
            notificationRequest.setRespondentName(caseData.getFullRespondentNameConsented());
            setCaseOrderType(notificationRequest, caseData);
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }
        if (caseData.isContestedApplication()) {
            notificationRequest.setRespondentName(caseData.getFullRespondentNameContested());
            notificationRequest.setSelectedCourt(CourtHelper.getSelectedFrc(caseDetails));
            log.info("selectedCourt is {} for case ID: {}", notificationRequest.getSelectedCourt(),
                notificationRequest.getCaseReferenceNumber());
        }
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        notificationRequest.setHearingType(listForHearingWrapper.getHearingType() != null
            ? listForHearingWrapper.getHearingType().getId()
            : "");

        if (caseDataKeysWrapper != null) {
            notificationRequest.setName(caseDataKeysWrapper.getSolicitorNameKey());
            notificationRequest.setNotificationEmail(caseDataKeysWrapper.getSolicitorEmailKey());
            notificationRequest.setSolicitorReferenceNumber(Objects.toString(caseDataKeysWrapper.getSolicitorReferenceKey(), EMPTY_STRING));
            notificationRequest.setIsNotDigital(caseDataKeysWrapper.getSolicitorIsNotDigitalKey());
        }

        return notificationRequest;
    }

    public NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails, Barrister barrister) {
        return NotificationRequest.builder()
            .name(barrister.getName())
            .barristerReferenceNumber(barrister.getOrganisation().getOrganisationID())
            .caseReferenceNumber(caseDetails.getId().toString())
            .notificationEmail(barrister.getEmail())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .phoneOpeningHours(CTSC_OPENING_HOURS)
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for an intervener in a financial remedy case.
     *
     * <p>This method prepares the notification details such as the intervener's full name,
     * solicitor firm, solicitor reference number, recipient name and email, and party names
     * from the case. If the intervener organisation name is present, it is used as the
     * solicitor firm name; otherwise, it falls back to the solicitor firm string directly.</p>
     *
     * @param caseDetails      the details of the financial remedy case, including applicant and respondent names
     * @param intervenerDetails the details of the intervener, including organisation and solicitor information
     * @param recipientName     the name of the recipient for the notification (can be null or empty)
     * @param recipientEmail    the email address of the notification recipient
     * @param referenceNumber   the solicitor's reference number (can be null or empty)
     * @return a populated {@link NotificationRequest} containing the relevant notification fields
     */
    public NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                                        String recipientName, String recipientEmail, String referenceNumber) {
        String intvSolicitorFirm = intervenerDetails.getIntervenerSolicitorFirm();
        String organisationName = Optional.of(intervenerDetails)
            .map(IntervenerDetails::getIntervenerOrganisation)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationName)
            .orElse(null);

        return NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getId().toString())
            .intervenerFullName(intervenerDetails.getIntervenerName())
            .intervenerSolicitorFirm(organisationName == null ? intvSolicitorFirm : organisationName)
            .intervenerSolicitorReferenceNumber(nullToEmpty(referenceNumber))
            .name(nullToEmpty(recipientName))
            .notificationEmail(recipientEmail)
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .phoneOpeningHours(CTSC_OPENING_HOURS)
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for sending a general email notification.
     * @param caseDetails the case details
     * @return the populated notification request
     */
    public NotificationRequest getNotificationRequestForGeneralEmail(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        SolicitorCaseDataKeysWrapper solicitorCaseData = getApplicantSolicitorCaseData(caseData);

        return notificationRequestBuilder()
            .withCaseDefaults(caseDetails)
            .withSolicitorCaseData(solicitorCaseData)
            .notificationEmail(caseData.getGeneralEmailWrapper().getGeneralEmailRecipient())
            .generalEmailBody(caseData.getGeneralEmailWrapper().getGeneralEmailBody())
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for a "stop representing client" email
     * to a solicitor identified by the given {@link CaseRole}.
     *
     * <p>
     * The solicitor details are resolved from the case data based on the supplied
     * role (applicant or respondent solicitor). The resulting notification request
     * is populated with standard case defaults, solicitor-specific fields, and
     * the date of issue.
     * </p>
     *
     * @param caseDetails the Finrem case details
     * @param caseRole the role identifying which solicitor should receive the notification
     * @return the constructed {@link NotificationRequest}
     * @throws IllegalStateException if the provided {@code caseRole} is not supported
     */
    public NotificationRequest getNotificationRequestForStopRepresentingClientEmail(FinremCaseDetails caseDetails,
                                                                                    CaseRole caseRole) {
        return getNotificationRequestForStopRepresentingClientEmail(caseDetails, caseRole, null);
    }

    /**
     * Builds a {@link NotificationRequest} for a "stop representing client" email
     * to a solicitor identified by the given {@link CaseRole}.
     *
     * <p>
     * The solicitor details are resolved from the case data based on the supplied
     * role:
     * </p>
     * <ul>
     *   <li>Applicant or respondent solicitor for {@code APP_SOLICITOR} or {@code RESP_SOLICITOR}</li>
     *   <li>Intervener solicitor for {@code INTVR_SOLICITOR_*} roles, in which case an
     *   {@link IntervenerType} must be provided</li>
     * </ul>
     *
     * <p>
     * The resulting notification request is populated with standard case defaults,
     * solicitor-specific fields, the date of issue, and (when applicable) intervener
     * details.
     * </p>
     *
     * @param caseDetails the Finrem case details
     * @param caseRole the role identifying which solicitor should receive the notification
     * @param intervenerType the intervener type, required when the {@code caseRole}
     *                       represents an intervener solicitor; may be {@code null}
     *                       otherwise
     * @return the constructed {@link NotificationRequest}
     * @throws IllegalArgumentException if an intervener solicitor role is provided
     *                                  without an {@code intervenerType}
     * @throws IllegalStateException if the provided {@code caseRole} is not supported
     */
    public NotificationRequest getNotificationRequestForStopRepresentingClientEmail(FinremCaseDetails caseDetails,
                                                                                    CaseRole caseRole,
                                                                                    IntervenerType intervenerType) {
        FinremCaseData caseData = caseDetails.getData();
        SolicitorCaseDataKeysWrapper solicitorCaseData =
            switch (caseRole) {
                case APP_SOLICITOR -> getApplicantSolicitorCaseData(caseData);
                case RESP_SOLICITOR -> getRespondentSolicitorCaseData(caseData);
                case INTVR_SOLICITOR_1, INTVR_SOLICITOR_2, INTVR_SOLICITOR_3, INTVR_SOLICITOR_4
                    -> getIntervenerSolicitorCaseData(caseData, ofNullable(intervenerType)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "intervenerType must be provided for intervener solicitor roles"
                    )));
                default -> throw new IllegalStateException("Unexpected value: " + caseRole);
            };

        return notificationRequestBuilder()
            .withCaseDefaults(caseDetails)
            .withSolicitorCaseData(solicitorCaseData)
            .withDateOfIssue()
            .withIntervener(intervenerType != null
                ? caseData.getIntervenerById(intervenerType.getIntervenerId()) : null
            )
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for a "stop representing client" email
     * to the given {@link Barrister}.
     *
     * <p>
     * This is a convenience overload that delegates to
     * {@link #getNotificationRequestForStopRepresentingClientEmail(FinremCaseDetails, Barrister, IntervenerType)}
     * with no intervener context.
     * </p>
     *
     * @param caseDetails the Finrem case details
     * @param barrister the barrister who should receive the notification
     * @return the constructed {@link NotificationRequest}
     */
    public NotificationRequest getNotificationRequestForStopRepresentingClientEmail(FinremCaseDetails caseDetails,
                                                                                    Barrister barrister) {
        return getNotificationRequestForStopRepresentingClientEmail(caseDetails, barrister, null);
    }

    /**
     * Builds a {@link NotificationRequest} for a "stop representing client" email
     * to the given {@link Barrister}.
     *
     * <p>
     * The notification request is populated using the barrister's contact details
     * (email and name) along with standard case defaults, the date of issue, and
     * the solicitor reference from the case data. When an {@link IntervenerType}
     * is provided, the corresponding intervener details are also included.
     * </p>
     *
     * @param caseDetails the Finrem case details
     * @param barrister the barrister who should receive the notification
     * @param intervenerType the intervener type to associate with the notification;
     *                       may be {@code null} if not applicable
     * @return the constructed {@link NotificationRequest}
     */
    public NotificationRequest getNotificationRequestForStopRepresentingClientEmail(FinremCaseDetails caseDetails,
                                                                                    Barrister barrister,
                                                                                    IntervenerType intervenerType) {
        FinremCaseData caseData = caseDetails.getData();

        boolean isApplicantBarrister = intervenerType == null && caseData.getBarristerCollectionWrapper().getApplicantBarristers()
            .stream().map(BarristerCollectionItem::getValue)
            .anyMatch(b -> b.equals(barrister));

        boolean isRespondentBarrister = intervenerType == null && caseData.getBarristerCollectionWrapper().getRespondentBarristers()
            .stream().map(BarristerCollectionItem::getValue)
            .anyMatch(b -> b.equals(barrister));

        String solicitorReferenceKey = null;
        if (isApplicantBarrister) {
            solicitorReferenceKey = caseData.getContactDetailsWrapper().getSolicitorReference();
        } else if (isRespondentBarrister) {
            solicitorReferenceKey = caseData.getContactDetailsWrapper().getRespondentSolicitorReference();
        } else if (intervenerType != null) {
            solicitorReferenceKey = caseData.getIntervenerById(intervenerType.getIntervenerId())
                .getIntervenerSolicitorReference();
        }

        SolicitorCaseDataKeysWrapper solicitorCaseData = SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(barrister.getEmail())
            .solicitorNameKey(barrister.getName())
            .solicitorReferenceKey(nullToEmpty(solicitorReferenceKey))
            .build();

        return notificationRequestBuilder()
            .withCaseDefaults(caseDetails)
            .withSolicitorCaseData(solicitorCaseData)
            .withDateOfIssue()
            .withIntervener(intervenerType != null
                ? caseData.getIntervenerById(intervenerType.getIntervenerId()) : null
            )
            .build();
    }

    private void setCaseOrderType(NotificationRequest notificationRequest, FinremCaseData caseData) {
        if (consentedApplicationHelper.isVariationOrder(caseData)) {
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
}
