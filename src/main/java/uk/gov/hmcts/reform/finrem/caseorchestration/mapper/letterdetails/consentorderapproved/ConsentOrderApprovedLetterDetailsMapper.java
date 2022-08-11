package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ConsentOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionProvider;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.getYesOrNo;

@Component
public class ConsentOrderApprovedLetterDetailsMapper extends AbstractLetterDetailsMapper {

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public ConsentOrderApprovedLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                                   ObjectMapper objectMapper,
                                                   ConsentedApplicationHelper consentedApplicationHelper) {
        super(courtDetailsMapper, objectMapper);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        return ConsentOrderApprovedLetterDetails.builder()
            .civilPartnership(getYesOrNo(caseData.getCivilPartnership()))
            .applicantFirstName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLastName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFirstName(getRespondentFirstName(caseDetails))
            .respondentLastName(getRespondentLastName(caseDetails))
            .servePensionProviderResponsibility(getServePensionProviderResponsibility(caseData))
            .servePensionProvider(getYesOrNo(caseData.getServePensionProvider()))
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

    private String getRespondentFirstName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getContactDetailsWrapper().getRespondentFmName()
            : caseDetails.getCaseData().getContactDetailsWrapper().getAppRespondentFmName();
    }

    private String getRespondentLastName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getContactDetailsWrapper().getRespondentLname()
            : caseDetails.getCaseData().getContactDetailsWrapper().getAppRespondentLName();
    }

    private String getJudgeTitle(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getConsentOrderWrapper().getConsentSelectJudge()
            : Optional.ofNullable(caseDetails.getCaseData().getOrderDirectionJudge())
            .map(JudgeType::getValue).orElse(null);
    }

    private String getJudgeName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getConsentOrderWrapper().getConsentJudgeName()
            : caseDetails.getCaseData().getOrderDirectionJudgeName();
    }

    private String getDirectionDate(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? String.valueOf(caseDetails.getCaseData().getConsentOrderWrapper().getConsentDateOfOrder())
            : String.valueOf(caseDetails.getCaseData().getOrderDirectionDate());
    }
}
