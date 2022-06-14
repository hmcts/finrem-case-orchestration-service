package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@RequiredArgsConstructor
public abstract class AbstractLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    public static final String COR_APPLICANT = "applicant";
    private final AddresseeGeneratorService addresseeGeneratorService;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;

    public NoticeOfChangeLetterDetails generate(CaseDetails caseDetails, CaseDetails caseDetailsBefore,
                                                RepresentationUpdate representationUpdate,
                                                DocumentHelper.PaperNotificationRecipient recipient) {

        return NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(getSolicitorReference(caseDetails, caseDetailsBefore, representationUpdate))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .solicitorFirmName(getSolicitorFirmName(representationUpdate, caseDetails, caseDetailsBefore))
            .respondentName(getRespondentName(caseDetails))
            .courtDetails(getCourtDetails(caseDetails))
            .addressee(getAddressee(caseDetails, caseDetailsBefore, recipient, representationUpdate))
            .noticeOfChangeText(getNoticeOfChangeText())
            .build();
    }

    abstract ChangedRepresentative getChangeOfRepresentative(RepresentationUpdate representationUpdate);

    abstract String getNoticeOfChangeText();

    abstract String getSolicitorReference(CaseDetails caseDetails, CaseDetails caseDetailsBefore, RepresentationUpdate representationUpdate);

    protected String getSolicitorReference(CaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return isApplicant(representationUpdate) ? Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)) :
            Objects.toString(caseDetails.getData().get(RESP_SOLICITOR_REFERENCE));
    }


    abstract String getSolicitorFirmName(RepresentationUpdate representationUpdate,
                                         CaseDetails caseDetails,
                                         CaseDetails caseDetailsBefore);

    protected String getSolicitorFirmName(CaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return isApplicant(representationUpdate)
            ? nullToEmpty(caseDetails.getData().get(getAppSolicitorFirmKey(caseDetails)))
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_FIRM));
    }

    abstract CaseDetails getCaseDetailsToUse(CaseDetails caseDetails,
                                             CaseDetails caseDetailsBefore,
                                             DocumentHelper.PaperNotificationRecipient recipient);


    private Addressee getAddressee(CaseDetails caseDetails,
                                   CaseDetails caseDetailsBefore,
                                   DocumentHelper.PaperNotificationRecipient recipient,
                                   RepresentationUpdate representationUpdate) {
        return addresseeGeneratorService.generateAddressee(
            getCaseDetailsToUse(caseDetails, caseDetailsBefore, recipient),
            getChangeOfRepresentative(representationUpdate),
            recipient, representationUpdate.getParty());
    }

    private boolean isApplicant(RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equalsIgnoreCase(COR_APPLICANT);
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

    private String getAppSolicitorFirmKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_SOLICITOR_FIRM
            : CONTESTED_SOLICITOR_FIRM;
    }

}
