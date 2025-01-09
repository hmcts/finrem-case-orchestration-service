package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrdersAboutToStartHandlerTest {

    @InjectMocks
    private UploadDraftOrdersAboutToStartHandler handler;

    @Mock
    private HearingService hearingService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void testHandle() {
        long caseID = 1727874196328932L;
        FinremCaseData caseData = spy(new FinremCaseData());

        DynamicList hearingDetails = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().label("test").code(UUID.randomUUID().toString()).build()
        )).build();

        when(caseData.getApplicantLastName()).thenReturn("Hello");
        when(caseData.getFullApplicantName()).thenReturn("Hello World");
        when(caseData.getRespondentLastName()).thenReturn("Hey");
        when(caseData.getRespondentFullName()).thenReturn("Hello Respondent");
        when(hearingService.generateSelectableHearingsAsDynamicList(any())).thenReturn(hearingDetails);

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(Collections.emptyList())
                .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        DraftOrdersWrapper draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        UploadAgreedDraftOrder uploadAgreedDraftOrder = draftOrdersWrapper.getUploadAgreedDraftOrder();
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = draftOrdersWrapper.getUploadSuggestedDraftOrder();

        assertThat(uploadAgreedDraftOrder.getHearingDetails()).isEqualTo(hearingDetails);

        DynamicMultiSelectList confirmUploadedDocuments = DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicMultiSelectListElement.builder()
            .label("I confirm the uploaded documents are for the Hello v Hey case")
            .code("1")
            .build())).build();
        assertThat(uploadAgreedDraftOrder.getConfirmUploadedDocuments()).isEqualTo(confirmUploadedDocuments);
        assertThat(uploadSuggestedDraftOrder.getConfirmUploadedDocuments()).isEqualTo(confirmUploadedDocuments);

        DynamicRadioList expectedUploadParty = DynamicRadioList.builder()
            .listItems(List.of(
                DynamicRadioListElement.builder()
                    .label("The applicant, Hello World").code("theApplicant").build(),
                DynamicRadioListElement.builder()
                    .label("The respondent, Hello Respondent").code("theRespondent").build()
            )).build();
        assertThat(uploadAgreedDraftOrder.getUploadParty()).isEqualTo(expectedUploadParty);
        assertThat(uploadSuggestedDraftOrder.getUploadParty()).isEqualTo(expectedUploadParty);
    }

    @Test
    void givenUserIsCaseWorkerWhenHandleThenShowUploadQuestionIsYes() {
        long caseID = 1727874196328932L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseID);
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(Collections.emptyList())
                .build());

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getShowUploadPartyQuestion()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenUserIsNotCaseWorkerWhenHandleThenShowUploadQuestionIsNo() {
        long caseID = 1727874196328932L;
        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseID);
        CaseAssignedUserRole caseAssignedUserRole = CaseAssignedUserRole.builder()
            .caseRole(CaseRole.APP_SOLICITOR.getCcdCode())
            .build();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(caseAssignedUserRole))
                .build());

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getShowUploadPartyQuestion()).isEqualTo(YesOrNo.NO);
    }

    @ParameterizedTest
    @MethodSource("testConsentApplicationGuidanceText")
    void testConsentApplicationGuidanceText(CaseRole caseRole, boolean textContainsLink) {
        long caseID = 1727874196328932L;
        CaseAssignedUserRole caseAssignedUserRole = CaseAssignedUserRole.builder()
            .caseRole(caseRole.getCcdCode())
            .build();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN)).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(caseAssignedUserRole))
                .build());

        FinremCallbackRequest request = FinremCallbackRequestFactory.fromId(caseID);
        var response = handler.handle(request, AUTH_TOKEN);
        String guidanceText = response.getData().getDraftOrdersWrapper().getConsentApplicationGuidanceText();

        String expectedGuidanceText = textContainsLink
            ? "Use the '<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/FR_consentOrder/FR_consentOrder1\">"
            + "consent order</a>' event if you need to upload a consent order to finalise the contested proceedings."
            : "Use the 'consent order' event if you need to upload a consent order to finalise the contested proceedings.";

        assertThat(guidanceText).isEqualTo(expectedGuidanceText);
    }

    private static Stream<Arguments> testConsentApplicationGuidanceText() {
        return Stream.of(
            Arguments.of(CaseRole.APP_SOLICITOR, true),
            Arguments.of(CaseRole.RESP_SOLICITOR, true),
            Arguments.of(CaseRole.APP_BARRISTER, true),
            Arguments.of(CaseRole.RESP_BARRISTER, true),
            Arguments.of(CaseRole.INTVR_BARRISTER_1, true),
            Arguments.of(CaseRole.INTVR_BARRISTER_2, true),
            Arguments.of(CaseRole.INTVR_BARRISTER_3, true),
            Arguments.of(CaseRole.INTVR_BARRISTER_4, true),
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, false),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, false),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, false),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, false)
        );
    }
}
