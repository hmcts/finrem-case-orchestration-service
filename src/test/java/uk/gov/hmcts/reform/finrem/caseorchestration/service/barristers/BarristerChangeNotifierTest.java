package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.BarristerLetterServiceAdapter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerTestUtils.createBarristerChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerTestUtils.createBarristers;

@ExtendWith(MockitoExtension.class)
class BarristerChangeNotifierTest {

    @InjectMocks
    private BarristerChangeNotifier barristerChangeNotifier;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BarristerLetterServiceAdapter barristerLetterServiceAdapter;

    @ParameterizedTest
    @MethodSource("barristerPartyData")
    void givenOnlyBarristersAdded_whenNotify_thenSendNotifications(BarristerParty barristerParty) {
        Set<Barrister> barristers = createBarristers();
        BarristerChange barristerChange = createBarristerChange(barristers, null);
        FinremCaseDetails caseDetails = createCaseDetails(barristerParty);

        barristerChangeNotifier.notify(new BarristerChangeNotifier.NotifierRequest(caseDetails, AUTH_TOKEN, barristerChange));

        // Verify an email was sent to each barrister
        barristers.forEach(barrister -> verify(notificationService).sendBarristerAddedEmail(caseDetails, barrister));

        // Verify letters are sent only to applicant or respondent
        if (barristerParty == BarristerParty.APPLICANT || barristerParty == BarristerParty.RESPONDENT) {
            barristers.forEach(barrister -> verify(barristerLetterServiceAdapter)
                .sendBarristerLetter(eq(caseDetails), eq(barrister),
                    any(BarristerLetterTuple.class), eq(AUTH_TOKEN)));
        }
    }

    @ParameterizedTest
    @MethodSource("barristerPartyData")
    void givenOnlyBarristersRemoved_whenNotify_thenSendNotifications(BarristerParty barristerParty) {
        Set<Barrister> barristers = createBarristers();
        BarristerChange barristerChange = createBarristerChange(null, barristers);
        FinremCaseDetails caseDetails = createCaseDetails(barristerParty);

        barristerChangeNotifier.notify(new BarristerChangeNotifier.NotifierRequest(caseDetails, AUTH_TOKEN, barristerChange));

        // Verify an email was sent to each barrister
        barristers.forEach(barrister -> verify(notificationService).sendBarristerRemovedEmail(caseDetails, barrister));

        // Verify letters are sent only to applicant or respondent
        if (barristerParty == BarristerParty.APPLICANT || barristerParty == BarristerParty.RESPONDENT) {
            barristers.forEach(barrister -> verify(barristerLetterServiceAdapter)
                .sendBarristerLetter(eq(caseDetails), eq(barrister),
                    any(BarristerLetterTuple.class), eq(AUTH_TOKEN)));
        }
    }

    @ParameterizedTest
    @MethodSource("barristerPartyData")
    void givenBarristersAddedAndRemoved_whenNotify_thenSendNotifications(BarristerParty barristerParty) {
        Set<Barrister> barristersAdded = createBarristers();
        Set<Barrister> barristersRemoved = createBarristers();
        BarristerChange barristerChange = createBarristerChange(barristerParty, barristersAdded, barristersRemoved);
        FinremCaseDetails caseDetails = createCaseDetails(barristerParty);

        barristerChangeNotifier.notify(new BarristerChangeNotifier.NotifierRequest(caseDetails, AUTH_TOKEN, barristerChange));

        // Verify an email was sent to each barrister
        barristersAdded.forEach(barrister -> verify(notificationService).sendBarristerAddedEmail(caseDetails, barrister));
        barristersRemoved.forEach(barrister -> verify(notificationService).sendBarristerRemovedEmail(caseDetails, barrister));

        // Verify letters are sent only to applicant or respondent
        if (barristerParty == BarristerParty.APPLICANT || barristerParty == BarristerParty.RESPONDENT) {
            barristersAdded.forEach(barrister -> verify(barristerLetterServiceAdapter)
                .sendBarristerLetter(eq(caseDetails), eq(barrister),
                    any(BarristerLetterTuple.class), eq(AUTH_TOKEN)));
            barristersRemoved.forEach(barrister -> verify(barristerLetterServiceAdapter)
                .sendBarristerLetter(eq(caseDetails), eq(barrister),
                    any(BarristerLetterTuple.class), eq(AUTH_TOKEN)));
        } else {
            verifyNoInteractions(barristerLetterServiceAdapter);
        }
    }

    private static Stream<Arguments> barristerPartyData() {
        return Arrays.stream(BarristerParty.values()).map(Arguments::of);
    }

    private FinremCaseDetails createCaseDetails(BarristerParty barristerParty) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .barristerParty(barristerParty)
                .build())
            .build();
    }

}
