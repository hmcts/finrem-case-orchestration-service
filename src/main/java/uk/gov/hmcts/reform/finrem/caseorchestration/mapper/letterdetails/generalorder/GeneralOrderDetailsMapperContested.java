package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralOrderDetails;

@Component
public class GeneralOrderDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {

    private static final String GENERAL_ORDER_COURT_CONSENTED = "SITTING in private";
    private static final String GENERAL_ORDER_COURT_SITTING = "SITTING AT the Family Court at the ";
    private static final String GENERAL_ORDER_HEADER_ONE_CONTESTED = "In the Family Court";
    private static final String GENERAL_ORDER_HEADER_ONE_CONSENTED = "Sitting in the Family Court";
    private static final String GENERAL_ORDER_HEADER_TWO = "sitting in the";

    public GeneralOrderDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        return GeneralOrderDetails.builder()
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .generalOrderCourtSitting(GENERAL_ORDER_COURT_SITTING)
            .generalOrderHeaderOne(getHeaderOne(caseDetails))
            .generalOrderHeaderTwo(GENERAL_ORDER_HEADER_TWO)
            .generalOrderCourt(getGeneralOrderCourt(caseDetails, courtList))
            .generalOrderJudgeDetails(getGeneralOrderJudgeDetails(caseDetails))
            .generalOrderRecitals(caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderRecitals())
            .generalOrderDate(String.valueOf(caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderDate()))
            .generalOrderBodyText(caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderBodyText())
            .build();
    }

    private String getHeaderOne(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isConsentedApplication()
            ? GENERAL_ORDER_HEADER_ONE_CONSENTED
            : GENERAL_ORDER_HEADER_ONE_CONTESTED;
    }

    private String getGeneralOrderCourt(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        return caseDetails.getData().isConsentedApplication()
            ? GENERAL_ORDER_COURT_CONSENTED
            : courtDetailsMapper.getCourtDetails(courtList).getCourtName();
    }

    private String getGeneralOrderJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderJudgeType().getValue(),
            caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderJudgeName());
    }
}
