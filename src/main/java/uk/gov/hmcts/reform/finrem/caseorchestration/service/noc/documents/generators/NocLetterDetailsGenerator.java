package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@RequiredArgsConstructor
public class NocLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    public static final String COR_APPLICANT = "applicant";
    protected static final String SOLICITOR_ADD_NOC_TEXT =
        "Your notice of change has been completed successfully. You can now view your client's case.";
    protected static final String SOLICITOR_REVOKED_NOC_TEXT =
        "You've completed notice of acting on this, your access to this case has now been revoked.";

    private final AddresseeGeneratorService addresseeGeneratorService;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    private final PrdOrganisationService prdOrganisationService;

    public NoticeOfChangeLetterDetails generate(CaseDetails caseDetails, CaseDetails caseDetailsBefore,
                                                RepresentationUpdate representationUpdate,
                                                DocumentHelper.PaperNotificationRecipient recipient,
                                                NoticeType noticeType) {

        return NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(getSolicitorReference(noticeType == NoticeType.ADD ? caseDetails : caseDetailsBefore, representationUpdate))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .solicitorFirmName(getSolicitorFirmName(representationUpdate, noticeType))
            .respondentName(getRespondentName(caseDetails))
            .courtDetails(getCourtDetails(caseDetails))
            .addressee(getAddressee(noticeType, caseDetails, caseDetailsBefore, recipient, representationUpdate))
            .noticeOfChangeText(getNoticeOfChangeText(recipient, noticeType))
            .build();
    }

    private Addressee getAddressee(NoticeType noticeType,
                                   CaseDetails caseDetails,
                                   CaseDetails caseDetailsBefore,
                                   DocumentHelper.PaperNotificationRecipient recipient,
                                   RepresentationUpdate representationUpdate) {
        final boolean isAddNoticeType = noticeType == NoticeType.ADD;
        return addresseeGeneratorService.generateAddressee(
            isAddNoticeType ? caseDetails : caseDetailsBefore,
            isAddNoticeType ? representationUpdate.getAdded() : representationUpdate.getRemoved(),
            recipient, representationUpdate.getParty());
    }

    private String getRespondentName(CaseDetails caseDetails) {
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        return isConsentedApplication ? documentHelper.getRespondentFullNameConsented(caseDetails) :
            documentHelper.getRespondentFullNameContested(caseDetails);
    }

    private Map getCourtDetails(CaseDetails caseDetails) {
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        return isConsentedApplication ? buildConsentedFrcCourtDetails() : buildFrcCourtDetails(caseDetails.getData());
    }

    private String getNoticeOfChangeText(DocumentHelper.PaperNotificationRecipient recipient, NoticeType noticeType) {
        String solicitorText = noticeType == NoticeType.ADD ? SOLICITOR_ADD_NOC_TEXT : SOLICITOR_REVOKED_NOC_TEXT;

        return recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR ? solicitorText : null;
    }

    private String getSolicitorReference(CaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return isApplicant(representationUpdate) ? Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)) :
            Objects.toString(caseDetails.getData().get(RESP_SOLICITOR_REFERENCE));
    }

    private String getSolicitorFirmName(RepresentationUpdate representationUpdate, NoticeType noticeType) {

        return noticeType == NoticeType.ADD
            ? getSolicitorFirmNameFromOrganisationService(representationUpdate.getAdded().getOrganisation().getOrganisationID())
            : getSolicitorFirmNameFromOrganisationService(representationUpdate.getRemoved().getOrganisation().getOrganisationID());
    }

    private String getSolicitorFirmNameFromOrganisationService(String organisationId) {
        if (organisationId == null) {
            return "";
        }
        OrganisationsResponse organisationsResponse = prdOrganisationService.findOrganisationByOrgId(organisationId);
        if (organisationsResponse != null) {
            return organisationsResponse.getName();
        }
        return "";
    }

    private boolean isApplicant(RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equals(COR_APPLICANT);
    }
}
