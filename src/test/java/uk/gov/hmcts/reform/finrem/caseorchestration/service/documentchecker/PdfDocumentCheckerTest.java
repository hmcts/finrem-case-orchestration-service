package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

class PdfDocumentCheckerTest {
}

//package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;
//
//import com.google.common.net.MediaType;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
//import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.ApplicantNameDocumentContentChecker;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Objects;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class PdfAppRespValidatorTest {
//
//    @Test
//    void testDoesNotHandleWordDoc() throws DocumentContentCheckerException {
//        FinremCaseData caseData = new FinremCaseData();
//        MediaType mediaType = MediaType.MICROSOFT_WORD;
//        byte[] document = new byte[100];
//
//        ApplicantNameDocumentContentChecker validator = new ApplicantNameDocumentContentChecker();
//        assertThat(validator.isContentInvalid(caseData, mediaType, document)).isFalse();
//    }
//    @ParameterizedTest
//    @MethodSource
//    void testApplicantNameValidation(String applicantFirstName, String applicantLastName, boolean expectedIsInvalid)
//        throws IOException, DocumentContentCheckerException, URISyntaxException {
//        FinremCaseData caseData = FinremCaseData.builder()
//            .contactDetailsWrapper(ContactDetailsWrapper.builder()
//                .build()
//            )
//            .build();
//        byte[] document = loadTestFile("fixtures/documentcontentvalidation/generalOrder.pdf");
//
//        ApplicantNameDocumentContentChecker validator = new ApplicantNameDocumentContentChecker();
//
//        assertThat(validator.isContentInvalid(caseData, MediaType.PDF, document)).isEqualTo(expectedIsInvalid);
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
//}

