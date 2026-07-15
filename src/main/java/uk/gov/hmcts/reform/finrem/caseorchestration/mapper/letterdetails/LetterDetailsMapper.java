package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.BasicLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddresseeGeneratorUtils.generateAddressee;

@Component
@RequiredArgsConstructor
public class LetterDetailsMapper {
    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final ObjectMapper objectMapper;
    private final CourtDetailsMapper courtDetailsMapper;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final FeatureToggleService featureToggleService;

    public BasicLetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 CourtListWrapper courtList) {
        return BasicLetterDetails.builder()
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .addressee(generateAddressee(caseDetails, recipient))
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .ctscContactDetails(getCtscContactDetails())
            .letterDate(String.valueOf(LocalDate.now()))
            .reference(getReference(caseDetails.getData(), recipient))
            .caseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .orderType(consentedApplicationHelper.getOrderType(caseDetails.getData()))
            .accessCode(getAccessCodeForRecipient(caseDetails, recipient))
            .build();
    }

    /**
     * Builds a map of letter details for the given case and paper notification recipient,
     * using the default court list for the case region.
     *
     * <p>This is a convenience overload that delegates to
     * {@link #getLetterDetailsAsMap(FinremCaseDetails, DocumentHelper.PaperNotificationRecipient, CourtListWrapper)}
     * and automatically resolves the default court list from the case data.</p>
     *
     * @param caseDetails the financial remedy case details
     * @param recipient the paper notification recipient
     * @return a map containing the case details and letter template data, structured for document generation
     */
    public Map<String, Object> getLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                                     DocumentHelper.PaperNotificationRecipient recipient) {
        return getLetterDetailsAsMap(caseDetails, recipient,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());
    }

    /**
     * Builds a map of letter details for the given case, paper notification recipient,
     * and court list.
     *
     * <p>The returned map is structured to match the expected document template format,
     * with case data converted into a {@code Map<String, Object>} and wrapped under
     * a {@code caseDetails} root element.</p>
     *
     * @param caseDetails the financial remedy case details
     * @param recipient the paper notification recipient
     * @param courtList the court list to be used when building the letter details
     * @return a map containing the case identifier and letter template data,
     *         suitable for document generation
     */
    public Map<String, Object> getLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                                     DocumentHelper.PaperNotificationRecipient recipient,
                                                     CourtListWrapper courtList) {
        Map<String, Object> documentTemplateDetails =
            objectMapper.convertValue(buildLetterDetails(caseDetails, recipient, courtList),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, documentTemplateDetails,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getReference(FinremCaseData caseData, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            || recipient == DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR
            ? nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference())
            : nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference());
    }

    private CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();
    }

    private List<AccessCodeCollection> getRecipientAccessCode(
        FinremCaseData caseData,
        DocumentHelper.PaperNotificationRecipient recipient) {

        return switch (recipient) {
            case APPLICANT -> caseData.getApplicantAccessCodes();
            case RESPONDENT -> caseData.getRespondentAccessCodes();
            default -> throw new IllegalStateException(
                "Unsupported recipient type: " + recipient);
        };
    }

    private String getAccessCodeForRecipient(
        FinremCaseDetails caseDetails,
        DocumentHelper.PaperNotificationRecipient recipient) {

        FinremCaseData caseData = caseDetails.getData();

        if (shouldGenerateAccessCode(caseDetails, recipient)) {
            generateAccessCode(caseData, recipient);
        }

        List<AccessCodeCollection> accessCodes =
            getRecipientAccessCode(caseData, recipient);

        return Optional.ofNullable(accessCodes)
            .orElse(List.of())
            .stream()
            .filter(accessCode -> accessCode.getValue().getIsValid().isYes())
            .map(accessCode -> accessCode.getValue().getAccessCode())
            .findFirst()
            .orElse(null);
    }

    private boolean shouldGenerateAccessCode(
        FinremCaseDetails caseDetails,
        DocumentHelper.PaperNotificationRecipient recipient) {

        return featureToggleService.isFinremCitizenUiEnabled()
            && isContestedCase(caseDetails)
            && isSupportedRecipient(recipient)
            && isNotRepresented(caseDetails.getData(), recipient)
            && hasNoAccessCode(caseDetails.getData(), recipient);
    }

    private boolean isContestedCase(FinremCaseDetails caseDetails) {
        return CaseType.CONTESTED.getCcdType()
            .equalsIgnoreCase(Strings.nullToEmpty(caseDetails.getCaseType().getCcdType()));
    }

    private boolean isSupportedRecipient(DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT || recipient == RESPONDENT;
    }

    private boolean isNotRepresented(
        FinremCaseData caseData,
        DocumentHelper.PaperNotificationRecipient recipient) {

        return switch (recipient) {
            case APPLICANT -> !caseData.isApplicantRepresentedByASolicitor();
            case RESPONDENT -> !caseData.isRespondentRepresentedByASolicitor();
            default -> false;
        };
    }

    private boolean hasNoAccessCode(
        FinremCaseData caseData,
        DocumentHelper.PaperNotificationRecipient recipient) {

        List<AccessCodeCollection> accessCodes =
            getRecipientAccessCode(caseData, recipient);

        return accessCodes == null || accessCodes.isEmpty();
    }

    private void generateAccessCode(
        FinremCaseData caseData,
        DocumentHelper.PaperNotificationRecipient recipient) {

        switch (recipient) {
            case APPLICANT -> AccessCodeGenerator.setApplicantAccessCode(caseData);
            case RESPONDENT -> AccessCodeGenerator.setRespondentAccessCode(caseData);
            default -> throw new IllegalArgumentException(
                "Access code generation is not supported for recipient: " + recipient);
        }
    }

}
