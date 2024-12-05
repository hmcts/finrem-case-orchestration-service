package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedDraftOrderNotApprovedDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import static java.util.Optional.ofNullable;


@Component
public class ContestedDraftOrderNotApprovedDetailsMapper extends AbstractLetterDetailsMapper {

    public ContestedDraftOrderNotApprovedDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        DraftOrdersWrapper draftOrdersWrapper = caseDetails.getData().getDraftOrdersWrapper();
        return ContestedDraftOrderNotApprovedDetails.builder()
            .caseNumber(caseDetails.getId().toString())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .contestOrderNotApprovedRefusalReasons(draftOrdersWrapper.getRefusalOrderReason())
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .refusalOrderDate(String.valueOf(draftOrdersWrapper.getRefusalOrderRefusedDate()))
            .build();
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            ofNullable(caseDetails.getData().getRefusalOrderJudgeType()).map(JudgeType::getValue).orElse(""),
            caseDetails.getData().getRefusalOrderJudgeName());
    }

}
