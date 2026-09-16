package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class RemoveDuplicateDocumentsTaskTest {
    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    private RemoveDuplicateDocumentsTask removeDuplicateDocumentsTask;
    private static final String REFERENCE = "1234567890123456";

    @BeforeEach
    void setup() {
        removeDuplicateDocumentsTask = new RemoveDuplicateDocumentsTask(caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper);
        ReflectionTestUtils.setField(removeDuplicateDocumentsTask, "taskEnabled", true);
        ReflectionTestUtils.setField(removeDuplicateDocumentsTask, "csvFile", "test.csv");
        ReflectionTestUtils.setField(removeDuplicateDocumentsTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(removeDuplicateDocumentsTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenTaskDoesNotRun() {
        ReflectionTestUtils.setField(removeDuplicateDocumentsTask, "taskEnabled", false);
        removeDuplicateDocumentsTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
    }

    @Test
    void givenDuplicateDocuments_whenTaskRun_thenDuplicateDocumentsAreRemoved() {
        FinremCaseDetails submittedCase = runTaskWithDocuments(documentsWithDuplicates());

        assertThat(submittedCase.getData().getUploadAdditionalDocument())
            .extracting(doc -> doc.getValue().getAdditionalDocuments())
            .extracting(
                CaseDocument::getDocumentUrl,
                CaseDocument::getDocumentFilename,
                CaseDocument::getDocumentBinaryUrl
            )
            .containsExactly(
                tuple(
                    "http://dm-store/documents/111",
                    "Order 1.pdf",
                    "http://dm-store/documents/111/binary"
                ),
                tuple(
                    "http://dm-store/documents/222",
                    "Order 2.pdf",
                    "http://dm-store/documents/222/binary"
                )
            );
    }

    @ParameterizedTest
    @MethodSource("differentDocumentDetails")
    void givenDocumentDetailsDiffer_whenTaskRun_thenAllDocumentsAreRetained(
        String url,
        String filename,
        String binaryUrl
    ) {
        var doc1 = additionalDocument(
            "http://dm-store/documents/111",
            "Order.pdf",
            "http://dm-store/documents/111/binary"
        );

        var doc2 = additionalDocument(url, filename, binaryUrl);

        FinremCaseDetails submittedCase = runTaskWithDocuments(List.of(doc1, doc2));

        assertThat(submittedCase.getData().getUploadAdditionalDocument())
            .hasSize(2);
    }

    private static Stream<Arguments> differentDocumentDetails() {
        return Stream.of(
            Arguments.of(
                "http://dm-store/documents/222",
                "Order.pdf",
                "http://dm-store/documents/111/binary"
            ),
            Arguments.of(
                "http://dm-store/documents/111",
                "Different Order.pdf",
                "http://dm-store/documents/111/binary"
            ),
            Arguments.of(
                "http://dm-store/documents/111",
                "Order.pdf",
                "http://dm-store/documents/222/binary"
            )
        );
    }

    private List<UploadAdditionalDocumentCollection> documentsWithDuplicates() {
        return List.of(
            additionalDocument(
                "http://dm-store/documents/111",
                "Order 1.pdf",
                "http://dm-store/documents/111/binary"
            ),
            additionalDocument(
                "http://dm-store/documents/111",
                "Order 1.pdf",
                "http://dm-store/documents/111/binary"
            ),
            additionalDocument(
                "http://dm-store/documents/111",
                "Order 1.pdf",
                "http://dm-store/documents/111/binary"
            ),
            additionalDocument(
                "http://dm-store/documents/222",
                "Order 2.pdf",
                "http://dm-store/documents/222/binary"
            )
        );
    }

    private FinremCaseDetails runTaskWithDocuments(List<UploadAdditionalDocumentCollection> documents) {
        mockLoadCaseReferenceList();
        mockSystemUserToken();

        CaseDetails caseDetails = createCaseData(documents);

        mockSearchCases(caseDetails);
        StartEventResponse startEventResponse = mockStartEvent(caseDetails);

        removeDuplicateDocumentsTask.run();

        verify(ccdService).submitEventForCaseWorker(
            same(startEventResponse),
            eq(AUTH_TOKEN),
            eq(REFERENCE),
            eq(CONTESTED.getCcdType()),
            eq(AMEND_CASE_CRON.getCcdType()),
            eq("DFR-3693 CT RemoveDuplicateDocumentsTask"),
            any(String.class)
        );

        return finremCaseDetailsMapper.mapToFinremCaseDetails(
            startEventResponse.getCaseDetails()
        );
    }

    private CaseDetails createCaseData(List<UploadAdditionalDocumentCollection> documents) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .uploadAdditionalDocument(new ArrayList<>(documents))
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }

    private StartEventResponse mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(
            AUTH_TOKEN,
            REFERENCE,
            CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType()
        )).thenReturn(startEventResponse);

        return startEventResponse;
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("test.csv", "DUMMY_SECRET"))
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

    private UploadAdditionalDocumentCollection additionalDocument(String url, String filename, String binaryUrl) {
        return UploadAdditionalDocumentCollection.builder()
            .value(UploadAdditionalDocument.builder()
                .additionalDocuments(caseDocument(url, filename, binaryUrl))
                .build())
            .build();
    }
}
