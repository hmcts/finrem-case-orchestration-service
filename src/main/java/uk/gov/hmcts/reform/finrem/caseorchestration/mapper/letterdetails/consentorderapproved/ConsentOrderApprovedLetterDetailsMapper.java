package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ConsentOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

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

    private String getRespondentFirstName(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isContestedApplication()
            ? caseDetails.getData().getContactDetailsWrapper().getRespondentFmName()
            : caseDetails.getData().getContactDetailsWrapper().getAppRespondentFmName();
    }

    private String getRespondentLastName(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isContestedApplication()
            ? caseDetails.getData().getContactDetailsWrapper().getRespondentLname()
            : caseDetails.getData().getContactDetailsWrapper().getAppRespondentLName();
    }

    private String getJudgeTitle(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isContestedApplication()
            ? caseDetails.getData().getConsentOrderWrapper().getConsentSelectJudge()
            : Optional.ofNullable(caseDetails.getData().getOrderDirectionJudge())
            .map(JudgeType::getValue).orElse(null);
    }

    private String getJudgeName(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isContestedApplication()
            ? caseDetails.getData().getConsentOrderWrapper().getConsentJudgeName()
            : caseDetails.getData().getOrderDirectionJudgeName();
    }

    private String getDirectionDate(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isContestedApplication()
            ? String.valueOf(caseDetails.getData().getConsentOrderWrapper().getConsentDateOfOrder())
            : String.valueOf(caseDetails.getData().getOrderDirectionDate());
    }
}
