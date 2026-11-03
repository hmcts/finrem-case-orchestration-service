package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsServiceTest {

    private static final String GA_DIRECTIONS = "/fixtures/contested/general-application-direction-start-reset.json";

    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";

    @InjectMocks
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    private final ObjectMapper objectMapper = createObjectMapper();

    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;
    @Mock
    private CcdService ccdService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private DocumentConfiguration documentConfiguration;
    private CaseDetails caseDetails;

    @BeforeEach
    void setup() {
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        lenient().when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument(DOC_URL, FILE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    void givenContestedCase_whenDirectionEventExecutedReturnNoPreviousStateFound_thenHandlerReturnsPostStateNull() {
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO).build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        when(ccdService.getCcdEventDetailsOnCase(any(), any(), any())).thenReturn(new ArrayList<>());
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        assertNull(generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    void givenExistingGenAppDirections_whenAboutToStart_thenResetOldFields() {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsFromResource(GA_DIRECTIONS, objectMapper);
        FinremCaseData caseData = finremCaseDetails.getData();
        generalApplicationDirectionsService.resetGeneralApplicationDirectionsFields(caseData);

        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsHearingRequired()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsHearingTime()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsHearingTimeEstimate()).isNull();;
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsAdditionalInformation()).isNull();;
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsCourtOrderDate()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsJudgeType()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsJudgeName()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsRecitals()).isNull();
        assertThat(generalApplicationWrapper.getGeneralApplicationDirectionsTextFromJudge()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsRegionList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsLondonFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsHighCourtFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsNorthEastFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsMidlandsFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsNorthWestFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsSouthEastFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsSouthWestFrcList()).isNull();
        assertThat(caseData.getRegionWrapper().getGeneralApplicationRegionWrapper()
            .getGeneralApplicationDirectionsWalesFrcList()).isNull();
    }

    @Test
    void givenContestedCase_whenDirectionEventExecutedReturnPreviousStateFound_thenHandlerReturnsPreviousPostState() {
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        when(ccdService.getCcdEventDetailsOnCase(any(), any(), any())).thenReturn(new ArrayList<>());

        assertEquals("applicationIssued", generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    void givenContestedCase_whenDirectionEventExecutedAndAnySortOfHearingGoingOn_thenHandlerReturnsPostState() {

        List<CaseEventDetail> list = new ArrayList<>();
        CaseEventDetail.CaseEventDetailBuilder builder = CaseEventDetail.builder();
        builder.eventName("General Application Outcome");
        list.add(builder.build());

        builder = CaseEventDetail.builder();
        builder.eventName("List for Hearing");
        list.add(builder.build());

        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        caseDetails.getData().put(GENERAL_APPLICATION_PRE_STATE, "applicationIssued");
        when(ccdService.getCcdEventDetailsOnCase(AUTH_TOKEN, caseDetails, EventType.GENERAL_APPLICATION_DIRECTIONS_MH.getCcdType()))
            .thenReturn(list);
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        assertEquals(PREPARE_FOR_HEARING_STATE, generalApplicationDirectionsService.getEventPostState(finremCaseDetails, AUTH_TOKEN));
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener1() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener1-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        data.setIntervenerOne(IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .intervenerName("intervener1").build());
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener2() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener2-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        data.setIntervenerTwo(IntervenerTwo.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .intervenerName("intervener1").build());
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener3() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener3-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        data.setIntervenerThree(IntervenerThree.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .intervenerName("intervener1").build());
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForIntervener4() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("intervener4-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        data.setIntervenerFour(IntervenerFour.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .intervenerName("intervener1").build());
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);

        verify(bulkPrintService).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForApplicant() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("applicant-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, never()).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrintedForRespondent() {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(mapToBulkPrintDocument(
            convertToCaseDocument(caseDetails.getData().get(GENERAL_APPLICATION_DOCUMENT_LATEST))));
        GeneralApplicationWrapper wrapper = GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingRequired(YesOrNo.NO)
            .generalApplicationPreState("applicationIssued").generalApplicationReferDetail("respondent-referdetails").build();
        FinremCaseData data = FinremCaseData.builder().generalApplicationWrapper(wrapper).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
            .data(data).build();
        generalApplicationDirectionsService.submitCollectionGeneralApplicationDirections(finremCaseDetails, documents, AUTH_TOKEN);
        verify(bulkPrintService).printApplicantDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService).printRespondentDocuments(
            any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, never()).printIntervenerDocuments(
            any(IntervenerWrapper.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN), any());
    }

    @Test
    void givenHearingRequiredIsNo_whenGenerateGeneralApplicationDirections_shouldGenerateGeneralApplicationOrderDocument() {
        // Arrange
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, YES_VALUE);

        FinremCaseData caseData = FinremCaseData.builder().generalApplicationWrapper(
                GeneralApplicationWrapper.builder()
                    .generalApplicationDirectionsHearingRequired(YesOrNo.NO)
                    .build())
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        Optional<CaseDocument> expectedDocument = Optional.of(
            CaseDocument.builder()
                .documentFilename(FILE_NAME)
                .documentUrl(DOC_URL)
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()
        );

        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        CaseDetails caseDetailsCopy = mock(CaseDetails.class);
        when(documentHelper.deepCopy(caseDetails, CaseDetails.class)).thenReturn(caseDetailsCopy);

        // Act
        Optional<CaseDocument> result =
            generalApplicationDirectionsService.generateGeneralApplicationDirectionsDocumentIfNeeded(AUTH_TOKEN, finremCaseDetails);

        // Assert
        assertEquals(expectedDocument, result);
    }

    @Test
    void givenHearingRequiredIsYes_whenGenerateGeneralApplicationDirections_shouldGenerateGeneralApplicationHearingDocument() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder().generalApplicationWrapper(
            GeneralApplicationWrapper.builder()
                .generalApplicationDirectionsHearingRequired(YesOrNo.YES)
                .build())
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        // When a hearing is required, the general application directions document should not be generated
        Optional<CaseDocument> expectedDocument = Optional.empty();

        // Act
        Optional<CaseDocument> result =
            generalApplicationDirectionsService.generateGeneralApplicationDirectionsDocumentIfNeeded(AUTH_TOKEN, finremCaseDetails);

        // Assert
        assertEquals(expectedDocument, result);
    }

    private CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private BulkPrintDocument mapToBulkPrintDocument(CaseDocument caseDocument) {
        return BulkPrintDocument.builder()
            .binaryFileUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename())
            .build();
    }
}
