package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationOrderDetails;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class GeneralApplicationOrderDetailsMapper extends AbstractLetterDetailsMapper {

    public GeneralApplicationOrderDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        final GeneralApplicationWrapper generalApplication = caseData.getGeneralApplicationWrapper();
        final FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return GeneralApplicationOrderDetails.builder()
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .courtDetails(courtDetails)
            .letterDate(String.valueOf(LocalDate.now()))
            .civilPartnership(YesOrNo.getYesOrNo(caseData.getCivilPartnership()))
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
