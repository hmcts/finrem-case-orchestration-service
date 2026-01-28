package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;

@ExtendWith(MockitoExtension.class)
class FindCasesWithMissingDocsTaskTest {

    private static final String AUTH_TOKEN = "AUTH";
    private static final String REFERENCE = "1234567890123456";

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper =
        new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));

    private FindCasesWithMissingDocsTask task;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setup() {
        task = new FindCasesWithMissingDocsTask(
            caseReferenceCsvLoader,
            ccdService,
            systemUserService,
            finremCaseDetailsMapper,
            evidenceManagementDownloadService
        );

        ReflectionTestUtils.setField(task, "taskEnabled", true);
        ReflectionTestUtils.setField(task, "csvFile", "findCasesWithMissingDocs.csv");
        ReflectionTestUtils.setField(task, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(task, "caseTypeId", CaseType.CONTESTED.getCcdType());

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(FindCasesWithMissingDocsTask.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoInteractions() {
        ReflectionTestUtils.setField(task, "taskEnabled", false);

        task.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(evidenceManagementDownloadService);
    }

    @Test
    void givenCaseHasNoDocuments_whenTaskRun_thenNoDownloadCalls() {
        CaseDetails caseDetails = createCaseWithCollections(List.of());

        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        task.run();

        verify(evidenceManagementDownloadService, never()).download(anyString(), anyString());
        assertThat(logAppender.list)
            .anyMatch(e -> e.getLevel() == Level.INFO
                && e.getFormattedMessage().contains("Completed missing document scan for caseId=" + REFERENCE));
    }

    @Test
    void givenCaseHasTwoDocuments_whenTaskRun_thenDownloadsBothBinaryUrlsWithSysToken() {
        UploadCaseDocumentCollection doc1 = collectionWithDoc("1", CaseDocumentType.STATEMENT_OF_ISSUES,
            "a.pdf", "http://doc-url-a/b28d4f46-7eb3-4816-828d-49eec9ed1497", "http://doc-url-a/b28d4f46-7eb3-4816-828d-49eec9ed1497/binary");
        UploadCaseDocumentCollection doc2 = collectionWithDoc("2", CaseDocumentType.STATEMENT_OF_ISSUES,
            "b.pdf", "http://doc-url-b/2e66b42d-ac38-42c7-93ad-6e2ff8c2c851", "http://doc-url-b/2e66b42d-ac38-42c7-93ad-6e2ff8c2c851/binary");

        CaseDetails caseDetails = createCaseWithCollections(List.of(doc1, doc2));

        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        task.run();

        verify(systemUserService, atLeastOnce()).getSysUserToken();
        verify(evidenceManagementDownloadService).getDocumentMetaData(UUID.fromString("b28d4f46-7eb3-4816-828d-49eec9ed1497"), AUTH_TOKEN);
        verify(evidenceManagementDownloadService).getDocumentMetaData(UUID.fromString("2e66b42d-ac38-42c7-93ad-6e2ff8c2c851"), AUTH_TOKEN);
    }

    @Test
    void givenDocStoreReturns4xx_whenTaskRun_thenLogsSingleErrorWithAllMissingDocs() {
        UploadCaseDocumentCollection doc1 = collectionWithDoc("1", CaseDocumentType.STATEMENT_OF_ISSUES,
            "a.pdf", "http://doc-url-a/b28d4f46-7eb3-4816-828d-49eec9ed1497", "http://doc-url-a/b28d4f46-7eb3-4816-828d-49eec9ed1497/binary");
        UploadCaseDocumentCollection doc2 = collectionWithDoc("2", CaseDocumentType.STATEMENT_OF_ISSUES,
            "b.pdf", "http://doc-url-b/2e66b42d-ac38-42c7-93ad-6e2ff8c2c851", "http://doc-url-b/2e66b42d-ac38-42c7-93ad-6e2ff8c2c851/binary");

        CaseDetails caseDetails = createCaseWithCollections(List.of(doc1, doc2));

        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        when(evidenceManagementDownloadService.getDocumentMetaData(UUID.fromString("b28d4f46-7eb3-4816-828d-49eec9ed1497"), AUTH_TOKEN))
            .thenThrow(HttpClientErrorException.NotFound.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                null,
                null
            ));

        when(evidenceManagementDownloadService.getDocumentMetaData(UUID.fromString("2e66b42d-ac38-42c7-93ad-6e2ff8c2c851"), AUTH_TOKEN))
            .thenThrow(HttpClientErrorException.NotFound.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                null,
                null
            ));

        task.run();

        assertThat(logAppender.list)
            .anyMatch(e ->
                e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().contains("Missing documents detected (404)")
                    && e.getFormattedMessage().contains("caseId=" + REFERENCE)
                    && e.getFormattedMessage().contains("missingCount=2")
                    && e.getFormattedMessage().contains("url=http://doc-url-a/b28d4f46-7eb3-4816-828d-49eec9ed1497")
                    && e.getFormattedMessage().contains("url=http://doc-url-b/2e66b42d-ac38-42c7-93ad-6e2ff8c2c851")
            );
    }

    @Test
    void givenDocStoreThrowsUnexpectedException_whenTaskRun_thenLogsUnexpectedErrorWithDetails() {
        UploadCaseDocumentCollection doc = collectionWithDoc(
            "1",
            CaseDocumentType.STATEMENT_OF_ISSUES,
            "file1.pdf",
            "http://doc-url/b28d4f46-7eb3-4816-828d-49eec9ed1497",
            "http://binary-url/b28d4f46-7eb3-4816-828d-49eec9ed1497/binary"
        );

        CaseDetails caseDetails = createCaseWithCollections(List.of(doc));

        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        when(evidenceManagementDownloadService.getDocumentMetaData(UUID.fromString("b28d4f46-7eb3-4816-828d-49eec9ed1497"), AUTH_TOKEN))
            .thenThrow(new RuntimeException("boom"));

        task.run();

        assertThat(logAppender.list)
            .anyMatch(e ->
                e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().contains("Unexpected error downloading document:")
                    && e.getFormattedMessage().contains("caseId=" + REFERENCE)
                    && e.getFormattedMessage().contains("state=APPLICATION_ISSUED")
                    && e.getFormattedMessage().contains("collection=STATEMENT_OF_ISSUES")
                    && e.getThrowableProxy() != null
                    && e.getThrowableProxy().getClassName().equals(RuntimeException.class.getName())
                    && e.getThrowableProxy().getMessage().contains("boom")
            );

        verify(evidenceManagementDownloadService).getDocumentMetaData(UUID.fromString("b28d4f46-7eb3-4816-828d-49eec9ed1497"), AUTH_TOKEN);
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("findCasesWithMissingDocs.csv", "DUMMY_SECRET"))
            .thenReturn(List.of(caseReference));
    }

    private void mockSystemUserToken() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
    }

    private void mockSearchCases(CaseDetails caseDetails) {
        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();

        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CaseType.CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private CaseDetails createCaseWithCollections(List<UploadCaseDocumentCollection> collections) {
        UploadCaseDocumentWrapper wrapper = UploadCaseDocumentWrapper.builder()
            .fdrCaseDocumentCollection(new ArrayList<>(collections))
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .uploadCaseDocumentWrapper(wrapper)
            .state(State.APPLICATION_ISSUED.toString())
            .build();

        FinremCaseDetails finremDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(finremDetails);
    }

    private UploadCaseDocumentCollection collectionWithDoc(String id,
                                                           CaseDocumentType documentType,
                                                           String filename,
                                                           String documentUrl,
                                                           String binaryUrl) {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentUrl(documentUrl)
            .documentBinaryUrl(binaryUrl)
            .documentFilename(filename)
            .build();

        UploadCaseDocument uploadCaseDocument = UploadCaseDocument.builder()
            .caseDocumentType(documentType)
            .caseDocuments(caseDocument)
            .build();

        return UploadCaseDocumentCollection.builder()
            .id(id)
            .uploadCaseDocument(uploadCaseDocument)
            .build();
    }
}
