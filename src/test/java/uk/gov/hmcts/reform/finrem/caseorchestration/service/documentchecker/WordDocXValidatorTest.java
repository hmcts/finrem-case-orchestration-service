package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

class WordDocXValidatorTest {
//    @ParameterizedTest
//    @MethodSource
//    void testApplicantNameValidation(String applicantFmName, String applicantLastName, boolean expectedIsInvalid)
//        throws IOException, DocumentContentCheckerException, URISyntaxException {
//        FinremCaseData caseData = FinremCaseData.builder()
//            .contactDetailsWrapper(ContactDetailsWrapper.builder()
//                .applicantFmName(applicantFmName)
//                .applicantLname(applicantLastName)
//                .respondentFmName("Davey")
//                .respondentLname("Duck")
//                .build()
//            )
//            .build();
//        byte[] document = loadTestFile("fixtures/documentcontentvalidation/generalOrder.docx");
//
//        DocxDocumentChecker validator = new DocxDocumentChecker();
//
//        assertThat(validator.isContentInvalid(caseData, MediaType.MICROSOFT_WORD, document)).isEqualTo(expectedIsInvalid);
//    }
//
//    private static Stream<Arguments> testApplicantNameValidation() {
//        return Stream.of(
//            Arguments.of("Doris", "Duck", false),
//            Arguments.of("Davey", "Duck", false),
//            Arguments.of("Donald", "Duck", true),
//            Arguments.of("Bella", "Smith", true)
//        );
//    }
//
//    private byte[] loadTestFile(String filename) throws IOException, URISyntaxException {
//        return Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
//            .getContextClassLoader().getResource(filename)).toURI()));
//    }
}
