package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationsCategoriserTest extends BaseHandlerTestSetup {
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;

    @Mock
    FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        generalApplicationsCategoriser = new GeneralApplicationsCategoriser(featureToggleService);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    public void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.categorize(finremCaseData);
        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_1.getDocumentCategoryId()
            );
        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_3.getDocumentCategoryId()
            );
    }

    @Test
    public void testUncategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);
        assert finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );
    }

    public DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    protected FinremCaseData buildFinremCaseData() {
        CaseDocument firstGeneralApplicationDocument = CaseDocument.builder().documentFilename("InterimHearingNoticeApp1.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();

        GeneralApplicationItems firstgeneralApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2))
                .generalApplicationDocument(firstGeneralApplicationDocument).build();

        GeneralApplicationsCollection firstGeneralApplications = GeneralApplicationsCollection.builder().build();
        firstGeneralApplications.setValue(firstgeneralApplicationItems);
        firstGeneralApplications.setId(UUID.randomUUID());

        GeneralApplicationItems secondGeneralApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();

        GeneralApplicationsCollection secondGeneralApplications = GeneralApplicationsCollection.builder().build();
        secondGeneralApplications.setValue(secondGeneralApplicationItems);
        secondGeneralApplications.setId(UUID.randomUUID());

        CaseDocument thirdGeneralApplicationDocument = CaseDocument.builder().documentFilename("InterimHearingNoticeApp3.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();

        GeneralApplicationItems thirdGeneralApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(buildDynamicIntervenerList())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2))
                .generalApplicationDocument(thirdGeneralApplicationDocument).build();

        GeneralApplicationsCollection thirdGeneralApplications = GeneralApplicationsCollection.builder().build();
        thirdGeneralApplications.setValue(thirdGeneralApplicationItems);
        thirdGeneralApplications.setId(UUID.randomUUID());

        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        generalApplicationsCollection.add(firstGeneralApplications);
        generalApplicationsCollection.add(secondGeneralApplications);
        generalApplicationsCollection.add(thirdGeneralApplications);

        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .appRespGeneralApplications(generalApplicationsCollection)
                .intervener1GeneralApplications(generalApplicationsCollection)
                .build()).build();

    }
}
