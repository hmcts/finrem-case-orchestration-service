package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.MiniFormADetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.getYesOrNo;

@Component
public class MiniFormADetailsMapper extends AbstractLetterDetailsMapper {
    public MiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
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
            .solicitorName(caseData.getAppSolicitorName())
            .solicitorAddress(caseData.getAppSolicitorAddress())
            .solicitorFirm(contactDetails.getSolicitorFirm())
            .solicitorReference(contactDetails.getSolicitorReference());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setRespondentFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                         FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return builder
            .appRespondentFmName(contactDetails.getAppRespondentFmName())
            .appRespondentLName(contactDetails.getAppRespondentLName())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentPhone(contactDetails.getRespondentPhone())
            .respondentAddressConfidential(getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()))
            .appRespondentRep(getYesOrNo(contactDetails.getConsentedRespondentRepresented()));
    }

    private MiniFormADetails.MiniFormADetailsBuilder setNatureApplicationFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                                FinremCaseData caseData) {

        return builder
            .natureOfApplication2(getNatureOfApplication2ListAsString(caseData))
            .natureOfApplication3a(caseData.getNatureApplicationWrapper().getNatureOfApplication3a())
            .natureOfApplication3b(caseData.getNatureApplicationWrapper().getNatureOfApplication3b())
            .natureOfApplication5(getYesOrNo(caseData.getNatureApplicationWrapper().getNatureOfApplication5()))
            .natureOfApplication6(getNatureOfApplication6ListAsString(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7());
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
            .orderForChildrenQuestion1(getYesOrNo(caseData.getNatureApplicationWrapper().getOrderForChildrenQuestion1()));
    }

    private List<String> getNatureOfApplication2ListAsString(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplication2List = caseData.getNatureApplicationWrapper().getNatureOfApplication2();
        return Optional.ofNullable(natureOfApplication2List)
            .orElse(new ArrayList<>())
            .stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getNatureOfApplication6ListAsString(FinremCaseData caseData) {
        List<ChildrenOrder> childrenOrders = caseData.getNatureApplicationWrapper().getNatureOfApplication6();
        return Optional.ofNullable(childrenOrders)
            .orElse(new ArrayList<>())
            .stream()
            .map(ChildrenOrder::getValue)
            .collect(Collectors.toList());
    }
}
