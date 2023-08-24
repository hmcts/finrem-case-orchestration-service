package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ConsentOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
public class ConsentOrderApprovedLetterDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public ConsentOrderApprovedLetterDetailsMapperContested(CourtDetailsMapper courtDetailsMapper,
                                                            ObjectMapper objectMapper,
                                                            ConsentedApplicationHelper consentedApplicationHelper) {
        super(courtDetailsMapper, objectMapper);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        return ConsentOrderApprovedLetterDetails.builder()
            .civilPartnership(YesOrNo.getYesOrNo(caseData.getCivilPartnership()))
            .applicantFirstName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLastName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFirstName(getRespondentFirstName(caseDetails))
            .respondentLastName(getRespondentLastName(caseDetails))
            .servePensionProviderResponsibility(getServePensionProviderResponsibility(caseData))
            .servePensionProvider(YesOrNo.getYesOrNo(caseData.getServePensionProvider()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .orderDirectionDate(getDirectionDate(caseDetails))
            .orderDirectionJudge(getJudgeTitle(caseDetails))
            .orderDirectionJudgeName(getJudgeName(caseDetails))
            .servePensionProviderOther(nullToEmpty(caseData.getServePensionProviderOther()))
            .orderType(consentedApplicationHelper.getOrderType(caseData))
            .build();
    }

    private String getServePensionProviderResponsibility(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getServePensionProviderResponsibility())
            .map(PensionProvider::getValue)
            .orElse(null);
    }

    private String getRespondentFirstName(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return caseDetails.getData().getContactDetailsWrapper().getRespondentFmName();
    }

    private String getRespondentLastName(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return caseDetails.getData().getContactDetailsWrapper().getRespondentLname();
    }

    private String getJudgeTitle(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return caseDetails.getData().getConsentOrderWrapper().getConsentSelectJudge();
    }

    private String getJudgeName(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return caseDetails.getData().getConsentOrderWrapper().getConsentJudgeName();
    }

    private String getDirectionDate(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return String.valueOf(caseDetails.getData().getConsentOrderWrapper().getConsentDateOfOrder());
    }
}
