package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessCodeGeneratorTest {

    @Test
    void generateAccessCode_shouldReturn8CharString() {
        String code = AccessCodeGenerator.generateAccessCode();

        assertNotNull(code);
        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Z2-9]+")); // allowed chars in class
    }

    @Test
    void setAccessCode_shouldCreateApplicantAndRespondentAccessCodesWhenEmpty() {
        FinremCaseData data = new FinremCaseData();

        AccessCodeGenerator.setAccessCode(data);

        assertNotNull(data.getApplicantAccessCodes());
        assertNotNull(data.getRespondentAccessCodes());
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertEquals(1, data.getRespondentAccessCodes().size());

        AccessCodeEntry appEntry = data.getApplicantAccessCodes().getFirst().getValue();
        AccessCodeEntry respEntry = data.getRespondentAccessCodes().getFirst().getValue();

        assertValidEntry(appEntry);
        assertValidEntry(respEntry);
    }

    @Test
    void setAccessCode_shouldNotOverwriteExistingCodes() {
        AccessCodeEntry existingEntry = AccessCodeEntry.builder()
            .accessCode("ABCDEFGH")
            .createdAt(LocalDateTime.now().minusDays(1))
            .validUntil(LocalDateTime.now().plusDays(89))
            .isValid(YesOrNo.YES)
            .build();

        AccessCodeCollection existingCollection =
            AccessCodeCollection.builder().value(existingEntry).build();

        FinremCaseData data = new FinremCaseData();
        data.setApplicantAccessCodes(List.of(existingCollection));
        data.setRespondentAccessCodes(List.of(existingCollection));

        AccessCodeGenerator.setAccessCode(data);

        // Ensure no new entries added
        assertEquals(1, data.getApplicantAccessCodes().size());
        assertEquals(1, data.getRespondentAccessCodes().size());
        assertEquals("ABCDEFGH", data.getApplicantAccessCodes().getFirst().getValue().getAccessCode());
    }

    private void assertValidEntry(AccessCodeEntry entry) {
        assertNotNull(entry.getAccessCode());
        assertEquals(8, entry.getAccessCode().length());
        assertEquals(YesOrNo.YES, entry.getIsValid());

        LocalDateTime createdAt = entry.getCreatedAt();
        LocalDateTime validUntil = entry.getValidUntil();

        assertNotNull(createdAt);
        assertNotNull(validUntil);

        // validUntil must be 90 days after creation
        assertEquals(createdAt.plusDays(90).toLocalDate(), validUntil.toLocalDate());
    }
}
