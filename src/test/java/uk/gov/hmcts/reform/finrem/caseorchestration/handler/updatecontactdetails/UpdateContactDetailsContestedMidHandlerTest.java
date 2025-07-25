package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsContestedMidHandlerTest {

    @InjectMocks
    private UpdateContactDetailsContestedMidHandler udnerTest;

    @Mock
    private InternationalPostalService postalService;

    @Test
    void testCanHandle() {
        assertCanHandle(udnerTest, MID_EVENT, CONTESTED, UPDATE_CONTACT_DETAILS);
    }

//    @Test
//    void testHandle() {
}
