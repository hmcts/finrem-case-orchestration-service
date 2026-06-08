package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final String ERROR_TEMPLATE =
        "%s address details missing. Unable to complete %s until party address details are added to avoid failed postal notification.";

    private ValidatePostalAddressService validatePostalAddressService;
    private FinremCaseDetails caseDetails;

    @BeforeEach
    public void setUp() {
        validatePostalAddressService = new ValidatePostalAddressService();
        caseDetails = new FinremCaseDetails();
        FinremCaseData caseData = new FinremCaseData();
        ContactDetailsWrapper wrapper = new ContactDetailsWrapper();

        caseData.setContactDetailsWrapper(wrapper);
        caseDetails.setData(caseData);
    }

    @Test
    public void shouldReturnNoErrors_WhenBothRepresentedAndBothPostalAddresses_ArePresent() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(buildValidAddress());
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(buildValidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.isEmpty();
    }

    @Test
    public void shouldReturnNoErrors_WhenBothNotRepresentedAndPostalAddresses_ArePresent() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(buildValidAddress());
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(buildValidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.isEmpty();
    }

    @Test
    public void shouldReturnApplicantError_WhenApplicantNotRepresentedAndApplicantPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.contains(String.format(ERROR_TEMPLATE, "Applicant", EventType.NONE));
    }

    @Test
    public void shouldReturnApplicantSolicitorError_WhenApplicantIsRepresentedAndApplicantPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.contains(String.format(ERROR_TEMPLATE, "Applicant solicitor", EventType.NONE));
    }

    @Test
    public void shouldReturnRespondentError_WhenRespondentNotRepresentedAndRespondentPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.contains(String.format(ERROR_TEMPLATE, "Respondent", EventType.NONE));
    }

    @Test
    public void shouldReturnRespondentSolicitorError_WhenRespondentIsRepresentedAndRespondentPostalAddress_IsMissing() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.contains(String.format(ERROR_TEMPLATE, "Respondent solicitor", EventType.NONE));
    }

    @Test
    public void shouldReturnBothErrors_WhenBothApplicantAndRespondentNotRepresentedAndBothPostalAddresses_AreMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(buildInvalidAddress());
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.containsAll(List.of(
                String.format(ERROR_TEMPLATE, "Applicant", EventType.NONE),
                String.format(ERROR_TEMPLATE, "Respondent", EventType.NONE)
            )
        );
    }

    @Test
    public void shouldReturnBothErrors_WhenBothApplicantAndRespondentIsRepresentedAndBothPostalAddresses_AreMissing() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(buildInvalidAddress());
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(buildInvalidAddress());

        List<String> errors = validatePostalAddressService.validateRequiredPostalAddresses(caseDetails, EventType.NONE);

        assert errors.containsAll(List.of(
            String.format(ERROR_TEMPLATE, "Applicant solicitor", EventType.NONE),
            String.format(ERROR_TEMPLATE, "Respondent solicitor", EventType.NONE)
        ));
    }

    private static Address buildValidAddress() {
        return Address.builder()
            .addressLine1("Address Line 1")
            .addressLine2("Address Line 2")
            .addressLine3("Address Line 3")
            .county("County")
            .country("Country")
            .postTown("Post Town")
            .postCode("Post Code")
            .build();
    }

    private static Address buildInvalidAddress() {
        return Address.builder()
            .addressLine1("")
            .addressLine2("Address Line 2")
            .addressLine3("Address Line 3")
            .county("County")
            .country("Country")
            .postTown("Town")
            .postCode("")
            .build();
    }
}
