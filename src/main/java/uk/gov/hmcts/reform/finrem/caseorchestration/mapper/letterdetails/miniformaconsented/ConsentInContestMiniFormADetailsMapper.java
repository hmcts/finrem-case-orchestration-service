package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsentInContestMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    public ConsentInContestMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        MiniFormADetails.MiniFormADetailsBuilder builder = setApplicantFields(MiniFormADetails.builder(), caseDetails);
        builder = setRespondentFields(builder, caseDetails);
        builder = setNatureApplicationFields(builder, caseData);
        builder = setOtherData(builder, caseData);
        return builder.build();
    }

    private MiniFormADetails.MiniFormADetailsBuilder setApplicantFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                        FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return builder
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .appRespondentFmName(contactDetails.getRespondentFmName())
            .appRespondentLName(contactDetails.getRespondentLname())
            .solicitorName(caseData.getApplicantSolicitorName())
            .solicitorAddress(caseData.getApplicantSolicitorAddress())
            .solicitorFirm(contactDetails.getApplicantSolicitorFirm());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setRespondentFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                         FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return builder
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentPhone(contactDetails.getRespondentPhone())
            .appRespondentRep(contactDetails.getContestedRespondentRepresented().getYesOrNo())
            .respondentAddressConfidential(contactDetails.getRespondentAddressHiddenFromApplicant().getYesOrNo());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setNatureApplicationFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                                FinremCaseData caseData) {

        return builder
            .natureOfApplication2(getNatureOfApplication2ListAsString(caseData))
            .natureOfApplication3a(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationAddress())
            .natureOfApplication3b(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationMortgage())
            .natureOfApplication5(caseData.getConsentOrderWrapper().getConsentNatureOfApplication5().getYesOrNo())
            .natureOfApplication6(getNatureOfApplication6ListAsString(caseData))
            .natureOfApplication7(caseData.getConsentOrderWrapper().getConsentNatureOfApplication7());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setOtherData(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                  FinremCaseData caseData) {
        return builder
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()))
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(caseData.getAuthorisationFirm())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .orderForChildrenQuestion1(caseData.getConsentOrderWrapper().getConsentOrderForChildrenQuestion1().getYesOrNo());
    }

    private List<String> getNatureOfApplication2ListAsString(FinremCaseData caseData) {
        return  caseData.getConsentOrderWrapper().getConsentNatureOfApplicationChecklist()
            .stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getNatureOfApplication6ListAsString(FinremCaseData caseData) {
        return caseData.getConsentOrderWrapper().getConsentNatureOfApplication6()
            .stream()
            .map(ConsentNatureOfApplication::getId)
            .collect(Collectors.toList());
    }
}
