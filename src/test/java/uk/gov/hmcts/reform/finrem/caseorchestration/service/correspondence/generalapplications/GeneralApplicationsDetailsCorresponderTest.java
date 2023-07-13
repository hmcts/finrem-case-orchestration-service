package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalapplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationsDetailsCorresponderTest {

    GeneralApplicationsDetailsCorresponder generalApplicationsDetailsCorresponder;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;

    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GenericDocumentService genericDocumentService;
    private GeneralApplicationHelper generalApplicationHelper;

    private ObjectMapper objectMapper;
    private final CaseDocument gaDirectionsDocument =
        TestSetUpUtils.caseDocument("directions-test-url", "directions-test-binary-url", "directions-test-filename");
    private final CaseDocument gaDocument = TestSetUpUtils.caseDocument("ga-test-url", "ga-test-binary-url", "ga-test-filename");
    private final CaseDocument doDocument = TestSetUpUtils.caseDocument("do-test-url", "do-test-binary-url", "do-test-filename");
    private CaseDetails caseDetails;
    private static final String AUTHORISATION_TOKEN = "Bearer token";

    @Before
    public void setUpTest() {
        objectMapper = new ObjectMapper();
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        generalApplicationHelper = new GeneralApplicationHelper(objectMapper, genericDocumentService);
        generalApplicationsDetailsCorresponder =
            new GeneralApplicationsDetailsCorresponder(bulkPrintService, notificationService, finremCaseDetailsMapper, generalApplicationHelper);


    }

    @Test
    public void shouldGetDocumentsToPrint() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        List<BulkPrintDocument> result = generalApplicationsDetailsCorresponder.getDocumentsToPrint(caseDetails, AUTHORISATION_TOKEN);
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getBinaryFileUrl(), is(gaDirectionsDocument.getDocumentBinaryUrl()));
        assertThat(result.get(0).getFileName(), is(gaDirectionsDocument.getDocumentFilename()));
        assertThat(result.get(1).getBinaryFileUrl(), is(gaDocument.getDocumentBinaryUrl()));
        assertThat(result.get(1).getFileName(), is(gaDocument.getDocumentFilename()));
        assertThat(result.get(2).getBinaryFileUrl(), is(doDocument.getDocumentBinaryUrl()));
        assertThat(result.get(2).getFileName(), is(doDocument.getDocumentFilename()));
    }

    @Test
    public void shouldSendLettersToApplicantAndRespondent() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        generalApplicationsDetailsCorresponder.sendCorrespondence(caseDetails, "authToken");
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
    }

    @Test
    public void shouldSendLettersToIntervenersWhenPresent() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setIntervenerOneWrapper(IntervenerOneWrapper.builder().intervenerName("Intervener Name").intervenerAddress(Address.builder()
            .addressLine1("1 Intervener Street")
            .addressLine2("Address Line 2")
            .postCode("SW1 1AA")
            .build()).build());
        caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        generalApplicationsDetailsCorresponder.sendCorrespondence(caseDetails, "authToken");
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(CaseDetails.class),
            anyString(), anyList());
    }

    private FinremCaseDetails buildFinremCaseDetails() {
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(
                FinremCaseData.builder().generalApplicationWrapper(
                    GeneralApplicationWrapper.builder().generalApplications(
                        List.of(GeneralApplicationsCollection.builder().value(
                                GeneralApplicationItems.builder()
                                    .generalApplicationDirectionsDocument(gaDirectionsDocument)
                                    .generalApplicationDocument(gaDocument)
                                    .generalApplicationDraftOrder(doDocument)
                                    .build())
                            .build()
                        )).build()).build())
            .id(123L)
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .build();
        return finremCaseDetails;
    }
}
