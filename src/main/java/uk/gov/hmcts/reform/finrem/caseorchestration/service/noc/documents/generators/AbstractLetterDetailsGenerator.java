package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper mapper;

    public NoticeOfChangeLetterDetails generate(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore,
                                                RepresentationUpdate representationUpdate,
                                                DocumentHelper.PaperNotificationRecipient recipient) {

        return NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .caseNumber(String.valueOf(caseDetails.getId()))
            .reference(getSolicitorReference(caseDetails, caseDetailsBefore, representationUpdate))
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .solicitorFirmName(getSolicitorFirmName(representationUpdate, caseDetails, caseDetailsBefore))
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .courtDetails(getCourtDetails(caseDetails))
            .addressee(getAddressee(caseDetails, caseDetailsBefore, recipient, representationUpdate))
            .noticeOfChangeText(getNoticeOfChangeText())
            .build();
    }

    abstract ChangedRepresentative getChangeOfRepresentative(RepresentationUpdate representationUpdate);

    abstract String getNoticeOfChangeText();

    abstract String getSolicitorReference(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, RepresentationUpdate representationUpdate);

    protected String getSolicitorReference(FinremCaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return isApplicant(representationUpdate)
            ? caseDetails.getCaseData().getContactDetailsWrapper().getSolicitorReference()
            : caseDetails.getCaseData().getContactDetailsWrapper().getRespondentSolicitorReference();
    }


    abstract String getSolicitorFirmName(RepresentationUpdate representationUpdate,
                                         FinremCaseDetails caseDetails,
                                         FinremCaseDetails caseDetailsBefore);

    protected String getSolicitorFirmName(FinremCaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return isApplicant(representationUpdate)
            ? nullToEmpty(caseDetails.getCaseData().getAppSolicitorFirm())
            : nullToEmpty(caseDetails.getCaseData().getContactDetailsWrapper().getRespondentSolicitorFirm());
    }

    abstract FinremCaseDetails getCaseDetailsToUse(FinremCaseDetails caseDetails,
                                             FinremCaseDetails caseDetailsBefore,
                                             DocumentHelper.PaperNotificationRecipient recipient);


    private Addressee getAddressee(FinremCaseDetails caseDetails,
                                   FinremCaseDetails caseDetailsBefore,
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

    private Map<String, Object> getCourtDetails(FinremCaseDetails caseDetails) {
        CourtListWrapper courtList = caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList();
        return caseDetails.getCaseData().isConsentedApplication()
            ? buildConsentedFrcCourtDetails()
            : mapper.convertValue(courtDetailsMapper.getCourtDetails(courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
    }
}
