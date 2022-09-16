package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ManageBarristerContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageBarristerContestedAboutToSubmitHandler manageBarristerContestedAboutToSubmitHandler;

    @Test
    public void givenContestedManageBarristerEventAboutToSubmit_whenCanHandleCalled_thenReturnTrue() {
        assertThat(manageBarristerContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_BARRISTER),
            is(true));
    }

//    @Test
//    public givenCaseWithBarrister_whenBarristerRemoved_thenAddChangeOfRepresentationHistory(){
//
//
//    }

}
