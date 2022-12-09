package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;

@RunWith(MockitoJUnitRunner.class)
public class BarristerRepresentationCheckerTest {

    public static final String EMAIL_ONE = "email1@mail.com";
    public static final String EMAIL_TWO = "email2@mail.com";
    public static final String EMAIL_THREE = "email3@mail.com";
    public static final String EMAIL_FOUR = "email4@mail.com";

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BarristerRepresentationChecker barristerRepresentationChecker;

    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
    }


    @Test
    public void givenNoRepresentationHistory_thenReturnFalse() {
        caseData.put(REPRESENTATION_UPDATE_HISTORY, null);

        assertFalse(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails(EMAIL_FOUR)));
    }

    @Test
    public void givenUserHasNotBeenBarrister_whenHasUserBeenBarristerOnCase_thenReturnFalse() {
        caseData.put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory());

        assertFalse(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails(EMAIL_FOUR)));
    }

    @Test
    public void givenUserHasBeenSolicitorButNotBarrister_whenHasUserBeenBarristerOnCase_thenReturnFalse() {
        caseData.put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory());

        assertFalse(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails(EMAIL_TWO)));
    }

    @Test
    public void givenUserHasBeenAddedBarrister_whenHasUserBeenBarristerOnCase_thenReturnTrue() {
        caseData.put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory());

        assertTrue(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails(EMAIL_ONE)));
    }

    @Test
    public void givenUserHasBeenRemovedBarrister_whenHasUserBeenBarristerOnCase_thenReturnTrue() {
        caseData.put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory());

        assertTrue(barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, userDetails(EMAIL_THREE)));
    }

    private List<Element<RepresentationUpdate>> representationUpdateHistory() {
        return List.of(
            Element.element(
                UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .added(ChangedRepresentative.builder().email(EMAIL_ONE).build())
                    .via(MANAGE_BARRISTERS)
                    .build()
            ),
            Element.element(
                UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .removed(ChangedRepresentative.builder().email(EMAIL_TWO).build())
                    .via(NOTICE_OF_CHANGE)
                    .build()
            ),
            Element.element(
                UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .removed(ChangedRepresentative.builder().email(EMAIL_THREE).build())
                    .via(MANAGE_BARRISTERS)
                    .build()
            )
        );
    }

    private UserDetails userDetails(String email) {
        return UserDetails.builder()
            .email(email)
            .build();
    }
}