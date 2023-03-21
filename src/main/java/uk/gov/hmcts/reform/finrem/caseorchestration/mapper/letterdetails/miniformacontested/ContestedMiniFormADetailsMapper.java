package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPayment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPaymentChecklist;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamExemption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureOfApplicationSchedule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PropertyAdjustmentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedMiniFormADetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.getYesOrNo;

@Component
public class ContestedMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    public ContestedMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                           ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        ContactDetailsWrapper contactDetails = caseDetails.getData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getData();

        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder =
            setApplicantDetails(ContestedMiniFormADetails.builder(), contactDetails, caseData);

        builder = setRespondentDetails(builder, contactDetails, caseData);
        if (ObjectUtils.isNotEmpty(caseData.getScheduleOneWrapper())
            && ObjectUtils.isNotEmpty(caseData.getScheduleOneWrapper().getTypeOfApplication())
            && caseData.getScheduleOneWrapper().getTypeOfApplication().getValue().equals(TYPE_OF_APPLICATION_DEFAULT_TO)) {
            builder = setNatureApplicationDetails(builder, caseData);
        } else {
            builder = setNatureApplicationDetailsSchedule(builder, caseData);
        }
        builder = setCoreCaseData(builder, caseData, contactDetails);
        builder = setMiamDetails(builder, caseData, caseDetails.getData().getMiamWrapper());
        builder = builder.caseNumber(caseDetails.getId().toString());

        return builder.build();
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setApplicantDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        ContactDetailsWrapper contactDetails,
        FinremCaseData caseData) {

        return builder
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .applicantAddress(contactDetails.getApplicantAddress())
            .applicantPhone(contactDetails.getApplicantPhone())
            .applicantEmail(contactDetails.getApplicantEmail())
            .applicantAddressConfidential(getYesOrNo(contactDetails.getApplicantAddressHiddenFromRespondent()))
            .applicantRepresented(getYesOrNo(contactDetails.getApplicantRepresented()))
            .applicantSolicitorName(caseData.getAppSolicitorName())
            .applicantSolicitorAddress(contactDetails.getApplicantSolicitorAddress())
            .applicantSolicitorFirm(contactDetails.getApplicantSolicitorFirm())
            .solicitorReference(contactDetails.getSolicitorReference());
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setRespondentDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        ContactDetailsWrapper contactDetails,
        FinremCaseData caseData) {

        return builder
            .respondentFmName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentPhone(contactDetails.getRespondentPhone())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentAddressConfidential(getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()))
            .respondentRepresented(getYesOrNo(contactDetails.getContestedRespondentRepresented()))
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail());
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setNatureApplicationDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData) {
        return builder
            .natureOfApplicationChecklist(getNatureOfApplicationChecklist(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7())
            .mortgageDetail(caseData.getMortgageDetail())
            .propertyAddress(caseData.getPropertyAddress())
            .propertyAdjustmentOrderDetail(getPropertyAdjustmentOrderDetailCollection(caseData))
            .paymentForChildrenDecision(getYesOrNo(caseData.getPaymentForChildrenDecision()))
            .benefitForChildrenDecision(caseData.getBenefitForChildrenDecision())
            .benefitPaymentChecklist(getBenefitPaymentChecklist(caseData));
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setNatureApplicationDetailsSchedule(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData) {
        return builder
            .natureOfApplicationChecklistSchedule(getNatureOfApplicationChecklistSchedule(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7())
            .mortgageDetail(caseData.getMortgageDetail())
            .propertyAddress(caseData.getPropertyAddress())
            .propertyAdjustmentOrderDetail(getPropertyAdjustmentOrderDetailCollection(caseData))
            .paymentForChildrenDecision(getYesOrNo(caseData.getPaymentForChildrenDecision()))
            .benefitForChildrenDecisionSchedule(caseData.getBenefitForChildrenDecisionSchedule())
            .benefitPaymentChecklistSchedule(getBenefitPaymentChecklistSchedule(caseData));
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setCoreCaseData(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData,
        ContactDetailsWrapper contactDetails) {
        return builder
            .fastTrackDecision(getYesOrNo(caseData.getFastTrackDecision()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(contactDetails.getSolicitorFirm())
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()));
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setMiamDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData,
        MiamWrapper miamDetails) {
        return builder
            .claimingExemptionMiam(getYesOrNo(miamDetails.getClaimingExemptionMiam()))
            .familyMediatorMiam(getYesOrNo(miamDetails.getFamilyMediatorMiam()))
            .applicantAttendedMiam(getYesOrNo(miamDetails.getApplicantAttendedMiam()))
            .mediatorRegistrationNumber(Objects.toString(caseData.getMediatorRegistrationNumber(), null))
            .familyMediatorServiceName(Objects.toString(caseData.getFamilyMediatorServiceName(), null))
            .soleTraderName(Objects.toString(caseData.getSoleTraderName(), null))
            .miamExemptionsChecklist(getMiamExemptionsChecklist(caseData))
            .miamDomesticViolenceChecklist(getMiamDomesticViolenceChecklist(caseData))
            .miamUrgencyReasonChecklist(getMiamUrgencyReasonsChecklist(caseData))
            .miamPreviousAttendanceChecklist(getMiamPreviousAttendanceChecklist(miamDetails))
            .miamOtherGroundsChecklist(getMiamOtherGroundsChecklist(miamDetails));
    }

    private String getMiamOtherGroundsChecklist(MiamWrapper miamDetails) {
        return Optional.ofNullable(miamDetails.getMiamOtherGroundsChecklist())
            .map(MiamOtherGrounds::getText)
            .orElse(null);
    }

    private String getMiamPreviousAttendanceChecklist(MiamWrapper miamDetails) {
        return Optional.ofNullable(miamDetails.getMiamPreviousAttendanceChecklist())
            .map(MiamPreviousAttendance::getText)
            .orElse(null);
    }


    private List<String> getNatureOfApplicationChecklist(FinremCaseData caseData) {
        List<NatureApplication> natureApplicationList = caseData.getNatureApplicationWrapper()
            .getNatureOfApplicationChecklist();

        return Optional.ofNullable(natureApplicationList).orElse(Collections.emptyList()).stream()
            .map(NatureApplication::getText)
            .toList();
    }

    private List<String> getNatureOfApplicationChecklistSchedule(FinremCaseData caseData) {
        List<NatureOfApplicationSchedule> natureOfApplicationChecklistSchedule = caseData.getScheduleOneWrapper()
            .getNatureOfApplicationChecklistSchedule();

        return Optional.ofNullable(natureOfApplicationChecklistSchedule).orElse(Collections.emptyList()).stream()
            .map(NatureOfApplicationSchedule::getText)
            .toList();
    }

    private List<String> getBenefitPaymentChecklistSchedule(FinremCaseData caseData) {
        List<BenefitPaymentChecklist> benefitPaymentChecklistSchedule = caseData.getBenefitPaymentChecklistSchedule();

        return Optional.ofNullable(benefitPaymentChecklistSchedule).orElse(Collections.emptyList()).stream()
            .map(BenefitPaymentChecklist::getValue)
            .toList();
    }

    private List<String> getBenefitPaymentChecklist(FinremCaseData caseData) {
        List<BenefitPayment> benefitPaymentChecklist = caseData.getBenefitPaymentChecklist();

        return Optional.ofNullable(benefitPaymentChecklist).orElse(Collections.emptyList()).stream()
            .map(BenefitPayment::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamExemptionsChecklist(FinremCaseData caseData) {
        List<MiamExemption> miamExemptions = caseData.getMiamWrapper().getMiamExemptionsChecklist();

        return Optional.ofNullable(miamExemptions).orElse(Collections.emptyList()).stream()
            .map(MiamExemption::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamDomesticViolenceChecklist(FinremCaseData caseData) {
        List<MiamDomesticViolence> domesticViolenceCheklist = caseData.getMiamWrapper().getMiamDomesticViolenceChecklist();

        return Optional.ofNullable(domesticViolenceCheklist).orElse(Collections.emptyList()).stream()
            .map(MiamDomesticViolence::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamUrgencyReasonsChecklist(FinremCaseData caseData) {
        List<MiamUrgencyReason> miamUrgencyReasons = caseData.getMiamWrapper().getMiamUrgencyReasonChecklist();

        return Optional.ofNullable(miamUrgencyReasons).orElse(Collections.emptyList()).stream()
            .map(MiamUrgencyReason::getText)
            .collect(Collectors.toList());
    }

    private List<PropertyAdjustmentOrderCollection> getPropertyAdjustmentOrderDetailCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getPropertyAdjustmentOrderDetail()).orElse(Collections.emptyList());
    }
}
