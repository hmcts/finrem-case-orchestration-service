package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalorder;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralOrderDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralOrderDetailsMapperTest extends ContestedAbstractLetterDetailsMapperTest {

    public static final String CONTESTED_GENERAL_ORDER = "/fixtures/general-order-contested.json";
    public static final String CONSENTED_GENERAL_ORDER = "/fixtures/general-order-consented.json";

    private static final String GENERAL_ORDER_COURT_CONSENTED = "SITTING in private";
    private static final String GENERAL_ORDER_COURT_SITTING = "SITTING AT the Family Court at the ";
    private static final String GENERAL_ORDER_HEADER_ONE_CONTEST = "In the Family Court";
    private static final String GENERAL_ORDER_HEADER_ONE_CONSENTED = "Sitting in the Family Court";
    private static final String GENERAL_ORDER_HEADER_TWO = "sitting in the";

    @Autowired
    private GeneralOrderDetailsMapperContested generalOrderDetailsMapper;

    @Test
    public void givenValidCaseDataContested_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        setCaseDetails(CONTESTED_GENERAL_ORDER);
        DocumentTemplateDetails actual = generalOrderDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedContestedGeneralOrderDetails();


        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseDataConsented_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        setCaseDetails(CONSENTED_GENERAL_ORDER);
        DocumentTemplateDetails actual = generalOrderDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedConsentedGeneralOrderDetails();


        assertEquals(expected, actual);
    }

    private GeneralOrderDetails getExpectedContestedGeneralOrderDetails() {
        return GeneralOrderDetails.builder()
            .applicantName("Contested Applicant Name")
            .respondentName("Contested Respondent Name")
            .divorceCaseNumber("DD98D76543")
            .generalOrderDate("2020-06-01")
            .generalOrderCourt("Nottingham County Court And Family Court")
            .generalOrderBodyText("Test is dummy text for contested")
            .generalOrderCourtSitting(GENERAL_ORDER_COURT_SITTING)
            .generalOrderHeaderOne(GENERAL_ORDER_HEADER_ONE_CONTEST)
            .generalOrderJudgeDetails("Her Honour Judge Contested")
            .generalOrderHeaderTwo(GENERAL_ORDER_HEADER_TWO)
            .generalOrderRecitals("Contested Recitals")
            .build();
    }

    private GeneralOrderDetails getExpectedConsentedGeneralOrderDetails() {
        return GeneralOrderDetails.builder()
            .applicantName("Consented Applicant Name")
            .respondentName("Consented Respondent Name")
            .divorceCaseNumber("DD12D12345")
            .generalOrderDate("2020-01-01")
            .generalOrderCourt(GENERAL_ORDER_COURT_CONSENTED)
            .generalOrderBodyText("Test is dummy text for consented")
            .generalOrderCourtSitting(GENERAL_ORDER_COURT_SITTING)
            .generalOrderHeaderOne(GENERAL_ORDER_HEADER_ONE_CONSENTED)
            .generalOrderJudgeDetails("His Honour Judge Consented")
            .generalOrderHeaderTwo(GENERAL_ORDER_HEADER_TWO)
            .generalOrderRecitals("Consented Recitals")
            .build();
    }
}