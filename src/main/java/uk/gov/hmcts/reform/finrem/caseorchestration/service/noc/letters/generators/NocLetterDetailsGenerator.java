package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.address.AddresseeBuilderService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final AddresseeBuilderService addresseeBuilder;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;

    public enum NoticeType {
        ADD, REMOVE
    }

    public NoticeOfChangeLetterDetails generate(CaseDetails caseDetails,
                                                ChangeOfRepresentation changeOfRepresentation,
                                                DocumentHelper.PaperNotificationRecipient recipient,
                                                NoticeType noticeType) {

        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);

        return NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(getSolicitorReference(caseDetails, changeOfRepresentation))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .solicitorFirmName(getSolicitorFirmName(changeOfRepresentation, noticeType))
            .respondentName(isConsentedApplication ? documentHelper.getRespondentFullNameConsented(caseDetails) :
                documentHelper.getRespondentFullNameContested(caseDetails))
            .courtDetails(isConsentedApplication ? buildConsentedFrcCourtDetails() : buildFrcCourtDetails(caseDetails.getData()))
            .addressee(addresseeBuilder.buildAddressee(caseDetails, recipient))
            .build();
    }

    private String getSolicitorReference(CaseDetails caseDetails, ChangeOfRepresentation changeOfRepresentation) {
        return isApplicant(changeOfRepresentation) ? Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)) :
            Objects.toString(caseDetails.getData().get(RESP_SOLICITOR_REFERENCE));
    }

    private String getSolicitorFirmName(ChangeOfRepresentation changeOfRepresentation, NoticeType noticeType) {
        return noticeType == NoticeType.ADD ? changeOfRepresentation.getAdded().getOrganisation().getOrganisationName() :
            changeOfRepresentation.getRemoved().getOrganisation().getOrganisationName();
    }

    private boolean isApplicant(ChangeOfRepresentation changeOfRepresentation) {
        return changeOfRepresentation.getParty().equals(COR_APPLICANT);
    }
}
