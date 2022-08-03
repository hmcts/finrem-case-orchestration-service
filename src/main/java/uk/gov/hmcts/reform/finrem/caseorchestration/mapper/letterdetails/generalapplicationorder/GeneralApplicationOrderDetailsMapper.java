package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.GeneralApplicationWrapper;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.getYesOrNo;

@Component
public class GeneralApplicationOrderDetailsMapper extends AbstractLetterDetailsMapper {

    public GeneralApplicationOrderDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        final GeneralApplicationWrapper generalApplication = caseData.getGeneralApplicationWrapper();
        final FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return GeneralApplicationOrderDetails.builder()
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .courtDetails(courtDetails)
            .letterDate(String.valueOf(LocalDate.now()))
            .civilPartnership(getYesOrNo(caseData.getCivilPartnership()))
            .generalApplicationDirectionsCourtOrderDate(String.valueOf(generalApplication.getGeneralApplicationDirectionsCourtOrderDate()))
            .generalApplicationDirectionsJudgeName(generalApplication.getGeneralApplicationDirectionsJudgeName())
            .generalApplicationDirectionsJudgeType(caseData.nullToEmpty(getJudgeType(generalApplication)))
            .generalApplicationDirectionsTextFromJudge(generalApplication.getGeneralApplicationDirectionsTextFromJudge())
            .build();
    }

    private String getJudgeType(GeneralApplicationWrapper generalApplication) {
        return Optional.ofNullable(generalApplication.getGeneralApplicationDirectionsJudgeType())
            .map(JudgeType::getValue).orElse("");
    }
}
