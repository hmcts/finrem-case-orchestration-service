package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OptionIdToValueTranslator;
import uk.gov.hmcts.reform.finrem.ccd.domain.BenefitPayment;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.MiamWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContestedMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    public ContestedMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                           ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        MiamWrapper miamDetails = caseDetails.getCaseData().getMiamWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return ContestedMiniFormADetails.builder()
            //core case data
            .fastTrackDecision(caseData.getFastTrackDecision().getYesOrNo())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            //contact details
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .respondentFMName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .applicantAddress(contactDetails.getApplicantAddress())
            .respondentAddress(contactDetails.getRespondentAddress())
            .applicantPhone(contactDetails.getApplicantPhone())
            .respondentPhone(contactDetails.getRespondentPhone())
            .applicantEmail(contactDetails.getApplicantEmail())
            .respondentEmail(contactDetails.getRespondentEmail())
            .applicantAddressConfidential(getYesOrNo(contactDetails.getApplicantAddressHiddenFromRespondent()))
            .respondentAddressConfidential(getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()))
            .applicantRepresented(getYesOrNo(contactDetails.getApplicantRepresented()))
            .respondentRepresented(getYesOrNo(contactDetails.getContestedRespondentRepresented()))
            .applicantSolicitorName(caseData.getApplicantSolicitorName())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .applicantSolicitorAddress(contactDetails.getApplicantSolicitorAddress())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .applicantSolicitorFirm(contactDetails.getApplicantSolicitorFirm())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .solicitorReference(contactDetails.getSolicitorReference())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            //Miam, application details and nature of application
            .natureOfApplicationChecklist(getNatureOfApplicationChecklist(caseData))
            .mortgageDetail(caseData.getMortgageDetail())
            .propertyAddress(caseData.getPropertyAddress())
            .propertyAdjustmentOrderDetail(getPropertyAdjustmentOrderDetailCollectionAsMapList(caseData))
            .paymentForChildrenDecision(getYesOrNo(caseData.getPaymentForChildrenDecision()))
            .benefitForChildrenDecision(getYesOrNo(caseData.getBenefitForChildrenDecision()))
            .benefitPaymentChecklist(getBenefitPaymentChecklist(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7())
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(contactDetails.getSolicitorFirm())
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()))
            .claimingExemptionMiam(getYesOrNo(miamDetails.getClaimingExemptionMiam()))
            .familyMediatorMiam(miamDetails.getFamilyMediatorMiam().getYesOrNo())
            .applicantAttendedMiam(miamDetails.getApplicantAttendedMiam().getYesOrNo())
            .miamExemptionsChecklist(getMiamExemptionsChecklist(caseData))
            .miamDomesticViolenceChecklist(getMiamDomesticViolenceChecklist(caseData))
            .miamUrgencyReasonChecklist(getMiamUrgencyReasonsChecklist(caseData))
            .miamPreviousAttendanceChecklist(miamDetails.getMiamPreviousAttendanceChecklist().getText())
            .miamOtherGroundsChecklist(miamDetails.getMiamOtherGroundsChecklist().getText())
            .build();
    }

    private List<String> getNatureOfApplicationChecklist(FinremCaseData caseData) {
        List<NatureApplication> natureApplicationList = caseData.getNatureApplicationWrapper()
            .getNatureOfApplicationChecklist();

        return Optional.ofNullable(natureApplicationList).orElse(new ArrayList<>()).stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getBenefitPaymentChecklist(FinremCaseData caseData) {
        List<BenefitPayment> benefitPaymentChecklist = caseData.getBenefitPaymentChecklist();

        return Optional.ofNullable(benefitPaymentChecklist).orElse(new ArrayList<>()).stream()
            .map(BenefitPayment::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamExemptionsChecklist(FinremCaseData caseData) {
        List<MiamExemption> miamExemptions = caseData.getMiamWrapper().getMiamExemptionsChecklist();

        return Optional.ofNullable(miamExemptions).orElse(new ArrayList<>()).stream()
            .map(MiamExemption::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamDomesticViolenceChecklist(FinremCaseData caseData) {
        List<MiamDomesticViolence> domesticViolenceCheklist = caseData.getMiamWrapper().getMiamDomesticViolenceChecklist();

        return Optional.ofNullable(domesticViolenceCheklist).orElse(new ArrayList<>()).stream()
            .map(MiamDomesticViolence::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamUrgencyReasonsChecklist(FinremCaseData caseData) {
        List<MiamUrgencyReason> miamUrgencyReasons = caseData.getMiamWrapper().getMiamUrgencyReasonChecklist();

        return Optional.ofNullable(miamUrgencyReasons).orElse(new ArrayList<>()).stream()
            .map(MiamUrgencyReason::getText)
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPropertyAdjustmentOrderDetailCollectionAsMapList(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getPropertyAdjustmentOrderDetail()).orElse(new ArrayList<>()).stream()
            .map(detail -> objectMapper.convertValue(detail, new TypeReference<Map<String, Object>>() {}))
            .collect(Collectors.toList());
    }

    private String getYesOrNo(YesOrNo answer) {
        return YesOrNo.isYes(answer)
            ? YesOrNo.YES.getYesOrNo()
            : YesOrNo.NO.getYesOrNo();
    }
}
