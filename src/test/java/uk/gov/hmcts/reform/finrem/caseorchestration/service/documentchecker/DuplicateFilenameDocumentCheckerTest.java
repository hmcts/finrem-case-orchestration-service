package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

class DuplicateFilenameDocumentCheckerTest {

//    @Test
//    void givenFilenameExistsOnGeneralOrderLatestDocument_whenChecksFailCalled_thenReturnsTrue() throws DocumentContentCheckerException {
//        String filename = "generalOrder-123.pd";
//        CaseDocument caseDocument = CaseDocument.builder()
//            .documentFilename(filename)
//            .build();
//        FinremCaseData caseData = FinremCaseData.builder()
//            .generalOrderWrapper(GeneralOrderWrapper.builder()
//                .generalOrderLatestDocument(caseDocument)
//                .build())
//            .build();
//
//        CaseDocument caseDocumentToCheck = CaseDocument.builder()
//            .documentFilename(filename)
//            .build();
//
//        DuplicateFilenameDocumentChecker checker = new DuplicateFilenameDocumentChecker();
//
//        assertThat(checker.checksFail(caseDocumentToCheck, new byte[0], caseData)).isTrue();
//    }
//
//    @Test
//    void givenFilenameNotExistsOnGeneralOrderLatestDocument_whenChecksFailCalled_thenReturnsFalse() throws DocumentContentCheckerException {
//        String filename = "generalOrder-123.pdf";
//        CaseDocument caseDocument = CaseDocument.builder()
//            .documentFilename(filename)
//            .build();
//        FinremCaseData caseData = FinremCaseData.builder()
//            .generalOrderWrapper(GeneralOrderWrapper.builder()
//                .generalOrderLatestDocument(caseDocument)
//                .build())
//            .build();
//
//        CaseDocument caseDocumentToCheck = CaseDocument.builder()
//            .documentFilename("generalOrder-456.pdf")
//            .build();
//
//        DuplicateFilenameDocumentChecker checker = new DuplicateFilenameDocumentChecker();
//
//        assertThat(checker.getWarnings(caseDocumentToCheck, new byte[0], caseData)).isFalse();
//    }
}
