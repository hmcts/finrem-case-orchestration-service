package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class MiniFormADetailsMapper extends AbstractLetterDetailsMapper {
    public MiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return MiniFormADetails.builder()
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .appRespondentFmName(contactDetails.getAppRespondentFmName())
            .appRespondentLName(contactDetails.getAppRespondentLName())
            .solicitorName(caseData.getApplicantSolicitorName())
            .solicitorAddress(caseData.getApplicantSolicitorAddress())
            .solicitorFirm(contactDetails.getSolicitorFirm())
            .solicitorReference(contactDetails.getSolicitorReference())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentPhone(contactDetails.getRespondentPhone())
            .appRespondentRep(contactDetails.getConsentedRespondentRepresented().getYesOrNo())
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()))
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(caseData.getAuthorisationFirm())
            .natureOfApplication2(getNatureOfApplication2ListAsString(caseData))
            .natureOfApplication3a(caseData.getNatureApplicationWrapper().getNatureOfApplication3a())
            .natureOfApplication3b(caseData.getNatureApplicationWrapper().getNatureOfApplication3b())
            .natureOfApplication5(caseData.getNatureApplicationWrapper().getNatureOfApplication5().getYesOrNo())
            .natureOfApplication6(getNatureOfApplication6ListAsString(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .orderForChildrenQuestion1(caseData.getNatureApplicationWrapper().getOrderForChildrenQuestion1().getYesOrNo())
            .respondentAddressConfidential(contactDetails.getRespondentAddressConfidential().getYesOrNo())
            .build();
    }

    private List<String> getNatureOfApplication2ListAsString(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplication2List = caseData.getNatureApplicationWrapper().getNatureOfApplication2();
        return natureOfApplication2List.stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getNatureOfApplication6ListAsString(FinremCaseData caseData) {
        List<ChildrenOrder> childrenOrders = caseData.getNatureApplicationWrapper().getNatureOfApplication6();
        return childrenOrders.stream()
            .map(ChildrenOrder::getValue)
            .collect(Collectors.toList());
    }
}
