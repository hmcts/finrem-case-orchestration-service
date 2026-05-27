package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class RemoveLeadingDotFromGeneralEmailRecipientTaskTest {

    private RemoveLeadingDotFromGeneralEmailRecipientTask task;

    @BeforeEach
    void setUp() {
        task = new RemoveLeadingDotFromGeneralEmailRecipientTask(
            mock(CaseReferenceCsvLoader.class),
            mock(CcdService.class),
            mock(SystemUserService.class),
            mock(FinremCaseDetailsMapper.class)
        );
    }

    @Test
    void shouldRemoveLeadingDotFromTopLevelGeneralEmailRecipient() {
        FinremCaseDetails caseDetails = caseDetailsWith(
            ".Richard.Robinson@ejudiciary.net",
            null
        );

        task.executeTask(caseDetails);

        assertThat(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient())
            .isEqualTo("Richard.Robinson@ejudiciary.net");
    }

    @Test
    void shouldRemoveLeadingDotFromCollectionGeneralEmailRecipient() {
        GeneralEmailHolder holder = GeneralEmailHolder.builder()
            .generalEmailRecipient(".Richard.Robinson@ejudiciary.net")
            .build();

        GeneralEmailCollection collectionItem = GeneralEmailCollection.builder()
            .value(holder)
            .build();

        FinremCaseDetails caseDetails = caseDetailsWith(
            "nochange@example.com",
            List.of(collectionItem)
        );

        task.executeTask(caseDetails);

        assertThat(caseDetails.getData().getGeneralEmailWrapper()
            .getGeneralEmailCollection().get(0).getValue().getGeneralEmailRecipient())
            .isEqualTo("Richard.Robinson@ejudiciary.net");
    }

    @Test
    void shouldNotChangeEmailsWhenNoLeadingDot() {
        GeneralEmailHolder holder = GeneralEmailHolder.builder()
            .generalEmailRecipient("recipient@example.com")
            .build();

        GeneralEmailCollection collectionItem = GeneralEmailCollection.builder()
            .value(holder)
            .build();

        FinremCaseDetails caseDetails = caseDetailsWith(
            "top@example.com",
            List.of(collectionItem)
        );

        task.executeTask(caseDetails);

        assertThat(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient())
            .isEqualTo("top@example.com");
        assertThat(caseDetails.getData().getGeneralEmailWrapper()
            .getGeneralEmailCollection().get(0).getValue().getGeneralEmailRecipient())
            .isEqualTo("recipient@example.com");
    }

    @Test
    void shouldHandleNullCollectionSafely() {
        FinremCaseDetails caseDetails = caseDetailsWith(".top@example.com", null);

        task.executeTask(caseDetails);

        assertThat(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient())
            .isEqualTo("top@example.com");
        assertThat(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailCollection())
            .isNull();
    }

    private FinremCaseDetails caseDetailsWith(String topLevelEmail, List<GeneralEmailCollection> collection) {
        GeneralEmailWrapper wrapper = GeneralEmailWrapper.builder()
            .generalEmailRecipient(topLevelEmail)
            .generalEmailCollection(collection)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .generalEmailWrapper(wrapper)
            .build();

        return FinremCaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
    }

    @Test
    void shouldReturnConfiguredCaseListFileName() {
        ReflectionTestUtils.setField(task, "csvFile", "updateGeneralEmailRecipient-encrypted.csv");

        assertThat(task.getCaseListFileName()).isEqualTo("updateGeneralEmailRecipient-encrypted.csv");
    }

    @Test
    void shouldReturnConfiguredTaskEnabledFlag() {
        ReflectionTestUtils.setField(task, "taskEnabled", true);

        assertThat(task.isTaskEnabled()).isTrue();
    }

    @Test
    void shouldReturnConfiguredCaseType() {
        ReflectionTestUtils.setField(task, "caseTypeId", "FinancialRemedyContested");

        assertThat(task.getCaseType()).isEqualTo(CaseType.CONTESTED);
    }

    @Test
    void shouldReturnTaskName() {
        assertThat(task.getTaskName()).isEqualTo("SetGeneralEmailRecipientTask");
    }

    @Test
    void shouldReturnSummary() {
        assertThat(task.getSummary()).isEqualTo("DFR-5061 CT Fix generalEmailRecipient");
    }
}
