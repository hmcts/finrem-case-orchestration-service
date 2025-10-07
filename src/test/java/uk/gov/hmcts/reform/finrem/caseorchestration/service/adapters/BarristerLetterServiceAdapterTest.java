package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerLetterService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

class BarristerLetterServiceAdapterTest {

    @Test
    void testSendBarristerLetter() {
        BarristerLetterService barristerLetterService = mock(BarristerLetterService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        Barrister barrister = Barrister.builder().build();
        BarristerLetterTuple barristerLetterTuple = new BarristerLetterTuple(
            DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR,
            AUTH_TOKEN, BarristerChangeType.ADDED);

        BarristerLetterServiceAdapter adapter = new BarristerLetterServiceAdapter(barristerLetterService, finremCaseDetailsMapper);
        adapter.sendBarristerLetter(finremCaseDetails, barrister, barristerLetterTuple, AUTH_TOKEN);

        verify(barristerLetterService).sendBarristerLetter(caseDetails, barrister, barristerLetterTuple, AUTH_TOKEN);
    }
}
