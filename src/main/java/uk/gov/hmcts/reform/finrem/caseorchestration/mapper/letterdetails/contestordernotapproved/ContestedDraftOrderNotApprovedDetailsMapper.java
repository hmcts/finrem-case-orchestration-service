package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;


/**
 * Repurpose this mapper class to transform FinremCaseDetails and court list information
 * into a document template details object specific to refusal order scenario.
 */
@Slf4j
@Component
public class ContestedDraftOrderNotApprovedDetailsMapper extends AbstractLetterDetailsMapper {

    /**
     * Constructs an instance of ContestedDraftOrderNotApprovedDetailsMapper.
     *
     * @param courtDetailsMapper the mapper for court details
     * @param objectMapper       the object mapper for handling JSON operations
     */
    public ContestedDraftOrderNotApprovedDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    /**
     * Builds a document template details object for a contested draft order not approved.
     *
     * @param caseDetails the case details containing the required data
     * @param courtList   the court list wrapper containing court information
     * @return a populated {@link DocumentTemplateDetails} object
     */
    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        DraftOrdersWrapper draftOrdersWrapper = caseDetails.getData().getDraftOrdersWrapper();
        return ContestedDraftOrderNotApprovedDetails.builder()
            .caseNumber(caseDetails.getId().toString())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(draftOrdersWrapper, caseDetails))
            .contestOrderNotApprovedRefusalReasons(draftOrdersWrapper.getGeneratedOrderReason())
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .refusalOrderDate(String.valueOf(draftOrdersWrapper.getGeneratedOrderRefusedDate()))
            .build();
    }

    private String getJudgeDetails(DraftOrdersWrapper draftOrdersWrapper, FinremCaseDetails caseDetails) {
        if (draftOrdersWrapper.getGeneratedOrderJudgeType() == null) {
            log.warn("{} - Judge type was not captured and an empty string will be shown in the refusal order.",
                caseDetails.getId());
        }
        return Stream.of(
                ofNullable(draftOrdersWrapper.getGeneratedOrderJudgeType()).map(JudgeType::getValue).orElse(""),
                draftOrdersWrapper.getGeneratedOrderJudgeName()
            )
            .filter(StringUtils::isNotBlank) // Exclude empty or blank strings
            .collect(Collectors.joining(" "));
    }

}
