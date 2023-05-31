package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_DATE;


@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationHelperTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private GenericDocumentService service;
    private String caseId = "123123123";

    @Test
    public void givenDate_whenOjectToDateTimeIsNotNull_thenReturnLocalDate() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(GENERAL_ORDER_DATE, "2022-10-10");
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertEquals(helper.objectToDateTime(data.get(GENERAL_ORDER_DATE)), LocalDate.of(2022, 10, 10));
    }

    @Test
    public void givenDate_whenOjectToDateTimeIsNull_thenReturnNull() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.objectToDateTime(data.get(GENERAL_ORDER_DATE)));
    }

    @Test
    public void givenContestedCase_whenMigratingExistingGeneralApplicationAndCreatedByIsNull_thenReturnNull() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.migrateExistingGeneralApplication(data, AUTH_TOKEN, anyString()));
    }

    @Test
    public void givenContestedCase_whenRetrieveInitialGeneralApplicationDataCreatedByIsNull_thenReturnNull() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        assertNull(helper.retrieveInitialGeneralApplicationData(data, "any", AUTH_TOKEN, anyString()));
    }

    @Test
    public void givenContestedCase_whenRetrieveInitialGeneralApplicationDataCreatedByIsNotNull_thenReturnNull() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        caseData.put(GENERAL_APPLICATION_RECEIVED_FROM, "Applicant");
        caseData.put(GENERAL_APPLICATION_CREATED_BY, "Applicant");
        caseData.put(GENERAL_APPLICATION_HEARING_REQUIRED, "Yes");
        caseData.put(GENERAL_APPLICATION_TIME_ESTIMATE, "2 weeks");
        caseData.put(GENERAL_APPLICATION_SPECIAL_MEASURES, "None");
        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Approved");

        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        GeneralApplicationCollectionData data = helper.retrieveInitialGeneralApplicationData(caseData, "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems items = data.getGeneralApplicationItems();

        assertEquals("Applicant", items.getGeneralApplicationCreatedBy());
        assertEquals("Applicant", items.getGeneralApplicationReceivedFrom());
        assertEquals("Yes", items.getGeneralApplicationHearingRequired());
        assertEquals("2 weeks", items.getGeneralApplicationTimeEstimate());
        assertEquals("None", items.getGeneralApplicationSpecialMeasures());
        assertEquals("Approved", items.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Not Approved");
        GeneralApplicationCollectionData dataNotApproved = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsNa = dataNotApproved.getGeneralApplicationItems();
        assertEquals("Not Approved", itemsNa.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Other");
        GeneralApplicationCollectionData dataOther = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsOther = dataOther.getGeneralApplicationItems();
        assertEquals("Other", itemsOther.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Anything");
        GeneralApplicationCollectionData dataDefault = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsDefault = dataDefault.getGeneralApplicationItems();
        assertEquals("Other", itemsDefault.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, "Yes");

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Approved");
        GeneralApplicationCollectionData dataApproved = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsApp = dataApproved.getGeneralApplicationItems();
        assertEquals("Approved, Completed", itemsApp.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Not Approved");
        GeneralApplicationCollectionData dataNotApproved2 = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsNa2 = dataNotApproved2.getGeneralApplicationItems();
        assertEquals("Not Approved, Completed", itemsNa2.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Other");
        GeneralApplicationCollectionData dataOther2 = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsOther2 = dataOther2.getGeneralApplicationItems();
        assertEquals("Other, Completed", itemsOther2.getGeneralApplicationStatus());

        caseData.put(GENERAL_APPLICATION_OUTCOME_DECISION, "Anything");
        GeneralApplicationCollectionData dataDefault2 = helper.retrieveInitialGeneralApplicationData(caseData,
            "any", AUTH_TOKEN, caseId);
        GeneralApplicationItems itemsDefault2 = dataDefault2.getGeneralApplicationItems();
        assertEquals("Other", itemsDefault2.getGeneralApplicationStatus());
    }

    @Test
    public void giveCase_whenCaseDocumentIsNotPdf_thenConvertToPdf() {
        CallbackRequest callbackRequest = callbackRequest();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(new ObjectMapper(), service);
        CaseDocument docxDocment = helper.convertToCaseDocument(callbackRequest.getCaseDetails().getData().get("caseDocument"));
        when(service.convertDocumentIfNotPdfAlready(docxDocment, AUTH_TOKEN, caseId)).thenReturn(caseDocument());
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseDocument caseDocument = helper.getPdfDocument(helper.convertToCaseDocument(data.get("caseDocument")), AUTH_TOKEN, caseId);
        assertEquals(FILE_NAME, caseDocument.getDocumentFilename());
    }

    private CallbackRequest callbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseDocument", caseDocument(DOC_URL,"app_docs.docx", BINARY_URL));
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(caseData).build())
            .build();
    }
}