package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ValidatePostalAddressServiceTest {

    private static final EventType EVENT_NAME = EventType.GENERAL_APPLICATION_DIRECTIONS_MH;
    private static final String ERROR_TEMPLATE =
        "%s address details missing. Unable to complete %s until party address details are added to avoid failed postal notification.";

    @InjectMocks
    ValidatePostalAddressService validatePostalAddressService;

    private FinremCaseDetails caseDetails;

    @BeforeEach
    public void setUp() {
        Address applicantAddress = Address.builder()
            .addressLine1("Address Line 1")
            .addressLine2("Address Line 2")
            .addressLine3("Address Line 3")
            .county("County")
            .country("Country")
            .postTown("Post Town")
            .postCode("SW1A 1AA")
            .build();

        Address respondentAddress = Address.builder()
            .addressLine1("Address Line 1")
            .addressLine2("Address Line 2")
            .addressLine3("Address Line 3")
            .county("County")
            .country("Country")
            .postTown("Post Town")
            .postCode("EC1A 1AA")
            .build();

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantAddress(applicantAddress)
            .applicantResideOutsideUK(YesOrNo.NO)
            .respondentAddress(respondentAddress)
            .respondentResideOutsideUK(YesOrNo.NO)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .build();

        caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();
    }

    @Test
    public void shouldReturnEmpty_WhenBothApplicantAndRespondentPostalAddresses_ArePresent() {
        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EVENT_NAME);

        assert errors.isEmpty();
    }

    @Test
    public void shouldReturnApplicantErrorMessage_WhenApplicantPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(null);
        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EVENT_NAME);

        assert errors.contains(ERROR_TEMPLATE.formatted("Applicant", EVENT_NAME));
    }

    @Test
    public void shouldReturnRespondentErrorMessage_WhenRespondentPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(null);
        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EVENT_NAME);

        assert errors.contains(ERROR_TEMPLATE.formatted("Respondent", EVENT_NAME));
    }

    @Test
    public void shouldReturnBothErrorMessages_WhenBothApplicantAndRespondentPostalAddresses_AreMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(null);
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(null);
        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EVENT_NAME);

        assert errors.containsAll(List.of(
            ERROR_TEMPLATE.formatted("Applicant", EVENT_NAME),
            ERROR_TEMPLATE.formatted("Respondent", EVENT_NAME)
        ));
    }
}
