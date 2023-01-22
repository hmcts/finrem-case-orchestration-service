package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderApprovedControllerTest {

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private DocumentHelper documentHelper;

    private ObjectMapper mapper = new ObjectMapper();
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;
    @InjectMocks
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;


    @Before
    public void setup() {
        consentOrderApprovedDocumentService = new ConsentOrderApprovedDocumentService(genericDocumentService,
            documentConfiguration, documentHelper, mapper, caseDataService, consentedApplicationHelper);
    }


    @Test
    public void givenFinremCaseDetails_whenAddGeneratedApprConsOrderDocsToCase_thenGenerateAndAddDocsToCase() {
        CaseDocument uploadApproveOrder = CaseDocument.builder().documentFilename("testUploadAppOrder").build();
        FinremCaseData finremCaseData = FinremCaseData.builder().consentOrderWrapper(
                ConsentOrderWrapper.builder().uploadApprovedConsentOrder(uploadApproveOrder).build())
            .build();

        when(genericDocumentService.annexStampDocument(any(), any()))
            .thenReturn(uploadApproveOrder);
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(uploadApproveOrder);
        when(documentHelper.deepCopy(any(), any()))
            .thenReturn(toCaseDetails(FinremCaseDetails.builder().data(finremCaseData).build()));

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        consentOrderApprovedDocumentService.addGeneratedApprovedConsentOrderDocumentsToCase("AUTH_TOKEN",
            finremCaseDetails);

        assertThat(finremCaseDetails.getData().getApprovedOrderCollection().get(0).getApprovedOrder()
                .getConsentOrder().getDocumentFilename(),
            is("testUploadAppOrder"));
    }

    private CaseDetails toCaseDetails(FinremCaseDetails finremCaseDetails) {
        CaseDetails generateDocumentPayload = null;
        try {
            generateDocumentPayload = mapper.readValue(mapper.writeValueAsString(finremCaseDetails), CaseDetails.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return generateDocumentPayload;
    }
}
