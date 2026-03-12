package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollectionName;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TestIntervenerWrapper extends IntervenerWrapper {

    public TestIntervenerWrapper(String intervenerSolEmail, String intervenerSolFirm,
                                 String intervenerSolReference, String intervenerSolName,
                                 String intervenerSolPhone) {
        super();
        setIntervenerSolEmail(intervenerSolEmail);
        setIntervenerSolicitorFirm(intervenerSolFirm);
        setIntervenerSolicitorReference(intervenerSolReference);
        setIntervenerSolName(intervenerSolName);
        setIntervenerSolPhone(intervenerSolPhone);
    }

    @Override
    public String getIntervenerLabel() {
        return "";
    }

    @Override
    public IntervenerType getIntervenerType() {
        return null;
    }

    @Override
    public String getAddIntervenerCode() {
        return "";
    }

    @Override
    public String getAddIntervenerValue() {
        return "";
    }

    @Override
    public String getDeleteIntervenerCode() {
        return "";
    }

    @Override
    public String getDeleteIntervenerValue() {
        return "";
    }

    @Override
    public String getUpdateIntervenerValue() {
        return "";
    }

    @Override
    public CaseRole getIntervenerSolicitorCaseRole() {
        return null;
    }

    @Override
    public List<IntervenerHearingNoticeCollection> getIntervenerHearingNoticesCollection(FinremCaseData caseData) {
        return List.of();
    }

    @Override
    public IntervenerHearingNoticeCollectionName getIntervenerHearingNoticesCollectionName() {
        return null;
    }

    @Override
    public DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient() {
        return null;
    }

    @Override
    public IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        return null;
    }

    @Override
    public void removeIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        // for testing
    }
}

class IntervenerWrapperTest {

    private TestIntervenerWrapper intervener;

    @BeforeEach
    void setUp() {
        intervener = new TestIntervenerWrapper(
            "email@test.com",
            "Firm Ltd",
            "REF123",
            "John Doe",
            "0123456789"
        );
    }

    @Test
    void shouldClearAllIntervenerSolicitorFields() {
        // Act
        intervener.clearIntervenerSolicitorFields();

        // Assert
        assertAll("intervener solicitor fields",
            () -> assertThat(intervener.getIntervenerSolEmail()).isNull(),
            () -> assertThat(intervener.getIntervenerSolicitorFirm()).isNull(),
            () -> assertThat(intervener.getIntervenerSolicitorReference()).isNull(),
            () -> assertThat(intervener.getIntervenerSolName()).isNull(),
            () -> assertThat(intervener.getIntervenerSolPhone()).isNull()
        );
    }
}
