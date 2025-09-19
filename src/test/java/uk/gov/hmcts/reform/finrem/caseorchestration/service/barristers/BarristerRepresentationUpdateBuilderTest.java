package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class BarristerRepresentationUpdateBuilderTest {

    @InjectMocks
    private BarristerRepresentationUpdateBuilder builder;
    @Mock
    private IdamService idamService;

    @BeforeEach
    void setup() {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Test User");
    }

    @ParameterizedTest
    @MethodSource("barristersData")
    void testBuildBarristerAdded(BarristerParty barristerParty, String expectedClientName, String expectedParty) {
        FinremCaseData caseData = createCaseData(barristerParty);
        Barrister barrister = createBarrister();

        RepresentationUpdate representationUpdate = builder.buildBarristerAdded(
            new BarristerRepresentationUpdateBuilder.BarristerUpdateParams(caseData, AUTH_TOKEN, barristerParty, barrister));
        assertThat(representationUpdate.getRemoved()).isNull();
        assertThat(representationUpdate.getAdded().getName()).isEqualTo("barristerName");
        assertThat(representationUpdate.getAdded().getEmail()).isEqualTo("barristerEmail");
        assertThat(representationUpdate.getAdded().getOrganisation().getOrganisationName()).isEqualTo("barristerOrganisation");

        assertThat(representationUpdate)
            .returns("Test User", RepresentationUpdate::getBy)
            .returns(MANAGE_BARRISTERS, RepresentationUpdate::getVia)
            .returns(expectedClientName, RepresentationUpdate::getClientName)
            .returns(expectedParty, RepresentationUpdate::getParty);
    }

    @ParameterizedTest
    @MethodSource("barristersData")
    void testBuildBarristerRemoved(BarristerParty barristerParty, String expectedClientName, String expectedParty) {
        FinremCaseData caseData = createCaseData(barristerParty);
        Barrister barrister = createBarrister();

        RepresentationUpdate representationUpdate = builder.buildBarristerRemoved(
            new BarristerRepresentationUpdateBuilder.BarristerUpdateParams(caseData, AUTH_TOKEN, barristerParty, barrister));
        assertThat(representationUpdate.getAdded()).isNull();
        assertThat(representationUpdate.getRemoved().getName()).isEqualTo("barristerName");
        assertThat(representationUpdate.getRemoved().getEmail()).isEqualTo("barristerEmail");
        assertThat(representationUpdate.getRemoved().getOrganisation().getOrganisationName()).isEqualTo("barristerOrganisation");

        assertThat(representationUpdate)
            .returns("Test User", RepresentationUpdate::getBy)
            .returns(MANAGE_BARRISTERS, RepresentationUpdate::getVia)
            .returns(expectedClientName, RepresentationUpdate::getClientName)
            .returns(expectedParty, RepresentationUpdate::getParty);
    }

    private static Stream<Arguments> barristersData() {
        return Stream.of(
            Arguments.of(BarristerParty.APPLICANT, "applicantFirst applicantLast", APPLICANT),
            Arguments.of(BarristerParty.RESPONDENT, "respondentFirst respondentLast", RESPONDENT),
            Arguments.of(BarristerParty.INTERVENER1, "intervenerOne", INTERVENER),
            Arguments.of(BarristerParty.INTERVENER2, "intervenerTwo", INTERVENER),
            Arguments.of(BarristerParty.INTERVENER3, "intervenerThree", INTERVENER),
            Arguments.of(BarristerParty.INTERVENER4, "intervenerFour", INTERVENER)
        );
    }

    private FinremCaseData createCaseData(BarristerParty barristerParty) {
        return FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .barristerParty(barristerParty)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("applicantFirst")
                .applicantLname("applicantLast")
                .respondentFmName("respondentFirst")
                .respondentLname("respondentLast")
                .build())
            .intervenerOne(IntervenerOne.builder()
                .intervenerName("intervenerOne")
                .build()
            )
            .intervenerTwo(IntervenerTwo.builder()
                .intervenerName("intervenerTwo")
                .build()
            )
            .intervenerThree(IntervenerThree.builder()
                .intervenerName("intervenerThree")
                .build()
            )
            .intervenerFour(IntervenerFour.builder()
                .intervenerName("intervenerFour")
                .build()
            )
            .build();
    }

    private Barrister createBarrister() {
        return Barrister.builder()
            .name("barristerName")
            .email("barristerEmail")
            .organisation(Organisation.builder().organisationName("barristerOrganisation").build())
            .build();
    }
}
