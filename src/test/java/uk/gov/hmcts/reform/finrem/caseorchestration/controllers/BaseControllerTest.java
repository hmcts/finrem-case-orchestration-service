package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.State;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATIVE_UPDATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPDATE_CONTACT_DETAILS_EVENT;

public abstract class BaseControllerTest extends BaseTest {

    @Autowired protected WebApplicationContext applicationContext;
    @Autowired protected ObjectMapper objectMapper;

    protected MockMvc mvc;
    protected JsonNode requestContent;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    protected void doEmptyCaseDataSetUp() {
        loadRequestContentWith("/fixtures/empty-casedata.json");
    }

    protected void doValidCaseDataSetUp() {
        loadRequestContentWith("/fixtures/pba-validate.json");
    }

    protected void doValidConsentOrderApprovedSetup() {
        loadRequestContentWith("/fixtures/contested/consent-in-contested-application-approved.json");
    }

    protected void doValidCaseDataSetUpForPaperApplication() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-paper-application.json");
    }

    protected void doValidCaseDataSetUpNoPensionCollection() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-no-pension-collection.json");
    }

    protected void doMissingLatestConsentOrder() {
        loadRequestContentWith("/fixtures/hwf.json");
    }

    protected void doValidConsentInContestWithPensionData() {
        loadRequestContentWith("/fixtures/contested/consent-in-contested-with-pension.json");
    }

    protected void doValidRefusalOrder() {
        loadRequestContentWith("/fixtures/refusal-order-contested.json");
    }

    protected void doValidCaseDataSetUpForAdditionalHearing() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-additional-hearing.json");
    }

    protected uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest getCallbackRequest(String source) {
        return new FinremCallbackRequestDeserializer(objectMapper).deserialize(source);
    }

    protected uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest getCallbackRequest() {
        return uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder().caseData(FinremCaseData.builder().build()).build())
            .eventType(EventType.ALLOCATE_TO_JUDGE)
            .caseDetailsBefore(FinremCaseDetails.builder().caseData(FinremCaseData.builder().build()).build())
            .build();
    }

    protected CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).id(Long.valueOf(123)).data(caseData).build();
        return CallbackRequest.builder().eventId("FR_issueApplication").caseDetails(caseDetails).build();
    }

    protected String buildNewCallbackRequestString() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseData.setPaperApplication(YesOrNo.NO);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED)
            .id(123L).caseData(caseData).build();

        return objectMapper.writeValueAsString(
            uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }

    protected String buildNewCallbackRequestStringConsented() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseData.setPaperApplication(YesOrNo.NO);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONSENTED).id(123L)
            .caseData(caseData).build();
        return objectMapper.writeValueAsString(
            uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }

    protected String buildNewCallbackRequestStringNoAppSolConsent() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseData(caseData).build();
        return objectMapper.writeValueAsString(
            uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }

    protected uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest buildNewCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = new FinremCaseDetails(123, "x", State.APPLICATION_ISSUED,
            LocalDateTime.now(), 2, "200", LocalDateTime.now(),
            Classification.PUBLIC, caseData, CaseType.CONTESTED, 1);

        return new uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest(EventType.PREPARE_FOR_HEARING, caseDetails, caseDetails);
    }

    protected uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest buildNewCallbackRequestConsented() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = new FinremCaseDetails(123, "x", State.APPLICATION_ISSUED,
            LocalDateTime.now(), 2, "200", LocalDateTime.now(),
            Classification.PUBLIC, caseData, CaseType.CONSENTED, 1);

        return new uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest(EventType.PREPARE_FOR_HEARING, caseDetails, caseDetails);
    }

    protected String buildCallbackRequestString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(buildCallbackRequest());
    }

    protected CallbackRequest buildNoCCaseworkerCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId(UPDATE_CONTACT_DETAILS_EVENT)
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
    }

    protected String buildNoCCaseworkerCallbackRequestString() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setUpdateIncludesRepresentativeChange(YesOrNo.YES);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED).caseData(caseData).build();
        return objectMapper.writeValueAsString(
            uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
                .eventType(EventType.UPDATE_CONTACT_DETAILS)
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetails)
                .build());
    }

    protected CallbackRequest buildCallbackRequestWithBeforeCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).data(caseData).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(Long.valueOf(120)).data(caseData).build();
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

    protected String buildCallbackRequestWithBeforeCaseDetailsStringPaper() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setPaperApplication(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED)
            .id(123L).caseData(caseData).build();

        return buildCallbackRequestStringBase(caseDetails);
    }

    protected String buildCallbackRequestWithBeforeCaseDetailsString() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L).caseData(caseData).build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L).caseData(caseData).build();
        return objectMapper.writeValueAsString(uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build());
    }

    protected String buildCallbackRequestStringBase(FinremCaseDetails caseDetails) throws JsonProcessingException {
        return objectMapper.writeValueAsString(uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build());
    }

    protected CallbackRequest buildCallbackInterimRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, "abc@mailinator.com");
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, "YES");
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).data(caseData).build();
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build();
    }

    protected CaseDocument getCaseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl("http://doc1");
        caseDocument.setDocumentBinaryUrl("http://doc1/binary");
        caseDocument.setDocumentFilename("doc1");
        return caseDocument;
    }

    protected String resourceContentAsString(String resourcePath) {
        return readJsonNodeFromFile(resourcePath).toString();
    }

    protected void loadRequestContentWith(String jsonPath) {
        requestContent = readJsonNodeFromFile(jsonPath);
    }

    private JsonNode readJsonNodeFromFile(String jsonPath) {
        try {
            return objectMapper.readTree(
                new File(getClass()
                    .getResource(jsonPath)
                    .toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest getCallbackRequestEmptyCaseData() {
        return uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder().build())
            .eventType(EventType.ALLOCATE_TO_JUDGE)
            .caseDetailsBefore(FinremCaseDetails.builder().build())
            .build();
    }
}
