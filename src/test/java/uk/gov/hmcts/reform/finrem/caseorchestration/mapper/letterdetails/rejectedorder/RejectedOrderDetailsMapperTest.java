package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.RejectedOrderDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.TranslatedOrderRefusalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.TranslatedOrderRefusalDocumentCollection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder.RejectedOrderDetailsMapper.CONSENTED_COURT_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder.RejectedOrderDetailsMapper.CONTESTED_COURT_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder.RejectedOrderDetailsMapper.REFUSAL_ORDER_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetailsObject;

public class RejectedOrderDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String TEST_JSON_CONTESTED = "/fixtures/refusal-order-contested.json";
    public static final String TEST_JSON_CONSENTED = "/fixtures/refusal-order-consented.json";

    @Autowired
    private RejectedOrderDetailsMapper rejectedOrderDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON_CONTESTED);
    }

    @Test
    public void givenValidCaseDataContested_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        DocumentTemplateDetails expected = getExpectedContestedRejectedOrderDetails();

        DocumentTemplateDetails actual = rejectedOrderDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());


        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenValidCaseDataConsented_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        setCaseDetails(TEST_JSON_CONSENTED);
        DocumentTemplateDetails expected = getExpectedConsentedRejectedOrderDetails();

        DocumentTemplateDetails actual = rejectedOrderDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenEmptyOrNullFields_whenBuildDocumentTemplateDetails_thenDoNotThrowException() {
        FinremCaseDetails emptyDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONSENTED).data(FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).build()).build();

        DocumentTemplateDetails actual = rejectedOrderDetailsMapper.buildDocumentTemplateDetails(emptyDetails,
            emptyDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertNotNull(actual);
    }

    private void assertTemplateFields(DocumentTemplateDetails actual, DocumentTemplateDetails expected) {
        Arrays.stream(actual.getClass().getDeclaredFields())
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    if (field.getName().contains("orderRefusalCollectionNew")) {
                        TranslatedOrderRefusalDocumentCollection actualOrderRefusal =
                            ((List<TranslatedOrderRefusalDocumentCollection>) field.get(actual)).get(0);
                        TranslatedOrderRefusalDocumentCollection expectedOrderRefusal =
                            ((List<TranslatedOrderRefusalDocumentCollection>) field.get(expected)).get(0);
                        assertEquals(expectedOrderRefusal, actualOrderRefusal);
                    }
                    assertEquals(field.get(expected), field.get(actual));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException();
                }
            });
    }

    private RejectedOrderDetails getExpectedContestedRejectedOrderDetails() {
        return RejectedOrderDetails.builder()
            .divorceCaseNumber("DD98D76543")
            .applicantName("Contested Applicant Name")
            .respondentName("Contested Respondent Name")
            .refusalOrderHeader(REFUSAL_ORDER_HEADER)
            .courtName(CONTESTED_COURT_NAME + getCourtDetails().getCourtName())
            .courtDetails(getCourtDetails())
            .orderRefusalCollectionNew(getOrderRefusalCollectionNew())
            .civilPartnership("No")
            .orderType("consent")
            .build();
    }

    private RejectedOrderDetails getExpectedConsentedRejectedOrderDetails() {
        return RejectedOrderDetails.builder()
            .divorceCaseNumber("DD98D76543")
            .applicantName("Contested Applicant Name")
            .respondentName("Consented Respondent Korivi")
            .refusalOrderHeader(REFUSAL_ORDER_HEADER)
            .courtName(CONSENTED_COURT_NAME)
            .courtDetails(buildConsentedFrcCourtDetailsObject())
            .orderRefusalCollectionNew(getOrderRefusalCollectionNew())
            .civilPartnership("No")
            .orderType("consent")
            .build();
    }

    private CourtDetailsTemplateFields getCourtDetails() {
        DefaultCourtListWrapper courtListWrapper = new DefaultCourtListWrapper();
        courtListWrapper.setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }

    private List<TranslatedOrderRefusalDocumentCollection> getOrderRefusalCollectionNew() {
        return Collections.singletonList(
            TranslatedOrderRefusalDocumentCollection.builder()
                .value(
                    TranslatedOrderRefusalDocument.builder()
                        .orderRefusalAfterText("testAfterText")
                        .orderRefusal(List.of(
                            "Insufficient information provided – A",
                            "Insufficient information provided – C",
                            "Order does not appear fair",
                            "Transferred to Applicant home Court - A",
                            "Transferred to Applicant home Court - B"))
                        .orderRefusalOther("testOther")
                        .orderRefusalDocs(CaseDocument.builder().build().builder()
                            .documentBinaryUrl("http://doc1.binary")
                            .documentUrl("http://doc1")
                            .documentFilename("doc1")
                            .build())
                        .orderRefusalJudgeName("Contested")
                        .orderRefusalJudge(JudgeType.HIS_HONOUR_JUDGE.getValue())
                        .orderRefusalDate(LocalDate.of(2022,1, 1))
                        .orderRefusalAddComments("testComment")
                        .build())
                .build());
    }
}