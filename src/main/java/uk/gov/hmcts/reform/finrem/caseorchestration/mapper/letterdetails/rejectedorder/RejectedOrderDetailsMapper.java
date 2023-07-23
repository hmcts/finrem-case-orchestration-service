package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.RejectedOrderDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.TranslatedOrderRefusalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.TranslatedOrderRefusalDocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class RejectedOrderDetailsMapper extends AbstractLetterDetailsMapper {

    public static final String REFUSAL_ORDER_HEADER = "Sitting in the Family Court";
    public static final String CONTESTED_COURT_NAME = "SITTING AT the Family Court at the ";
    public static final String CONSENTED_COURT_NAME = "SITTING in private";

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public RejectedOrderDetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                      ObjectMapper objectMapper,
                                      ConsentedApplicationHelper consentedApplicationHelper) {
        super(courtDetailsMapper, objectMapper);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        FrcCourtDetails courtDetails = getFrcCourtDetails(caseData, courtList);
        return RejectedOrderDetails.builder()
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .civilPartnership(YesOrNo.getYesOrNo(caseData.getCivilPartnership()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .refusalOrderHeader(REFUSAL_ORDER_HEADER)
            .courtDetails(courtDetails)
            .courtName(getCourtName(caseData, courtDetails))
            .orderRefusalCollectionNew(getTranslatedRefusalOrderCollection(caseData))
            .orderType(consentedApplicationHelper.getOrderType(caseData))
            .build();
    }

    private FrcCourtDetails getFrcCourtDetails(FinremCaseData caseData, CourtListWrapper courtList) {
        return caseData.isConsentedApplication()
            ? getConsentedFrcCourtDetails()
            : courtDetailsMapper.getCourtDetails(courtList);
    }

    private String getCourtName(FinremCaseData caseData, FrcCourtDetails courtDetails) {
        return caseData.isContestedApplication()
            ? CONTESTED_COURT_NAME + courtDetails.getCourtName()
            : CONSENTED_COURT_NAME;
    }

    private List<TranslatedOrderRefusalDocumentCollection> getTranslatedRefusalOrderCollection(FinremCaseData caseData) {

        return Optional.ofNullable(caseData.getOrderRefusalCollectionNew()).orElse(new ArrayList<>())
            .stream()
            .map(refusalOrder -> TranslatedOrderRefusalDocumentCollection.builder()
                .value(TranslatedOrderRefusalDocument.builder()
                    .orderRefusal(getReasonsAsStringAndTranslate(refusalOrder))
                    .orderRefusalAddComments(refusalOrder.getValue().getOrderRefusalAddComments())
                    .orderRefusalAfterText(refusalOrder.getValue().getOrderRefusalAfterText())
                    .orderRefusalDate(refusalOrder.getValue().getOrderRefusalDate())
                    .orderRefusalJudge(getOrderRefusalJudge(refusalOrder))
                    .orderRefusalJudgeName(refusalOrder.getValue().getOrderRefusalJudgeName())
                    .orderRefusalDocs(refusalOrder.getValue().getOrderRefusalDocs())
                    .orderRefusalOther(refusalOrder.getValue().getOrderRefusalOther())
                    .build())
                .build()
            ).toList();
    }

    private List<String> getReasonsAsStringAndTranslate(OrderRefusalCollection refusalOrder) {
        List<String> orderRefusalStrings = refusalOrder.getValue().getOrderRefusal().stream()
            .map(OrderRefusalOption::getId).collect(toList());

        if (orderRefusalStrings.contains(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD.getId())) {
            orderRefusalStrings.addAll(List.of("Transferred to Applicant home Court - A",
                "Transferred to Applicant home Court - B"));
            orderRefusalStrings.remove(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD.getId());
        }
        return orderRefusalStrings;
    }

    private String getOrderRefusalJudge(OrderRefusalCollection refusalOrder) {
        return Optional.ofNullable(refusalOrder.getValue().getOrderRefusalJudge()).map(JudgeType::getValue)
            .orElse("");
    }

    private FrcCourtDetails getConsentedFrcCourtDetails() {
        return FrcCourtDetails.builder()
            .courtName(OrchestrationConstants.CTSC_COURT_NAME)
            .courtAddress(OrchestrationConstants.CTSC_COURT_ADDRESS)
            .phoneNumber(OrchestrationConstants.CTSC_PHONE_NUMBER)
            .email((OrchestrationConstants.CTSC_EMAIL_ADDRESS))
            .build();
    }
}
