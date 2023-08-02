package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener.IntervenerHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.capitalize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;

@Slf4j
@Service
public class CreateGeneralLetterAboutToStartHandler extends FinremCallbackHandler implements IntervenerHandler {

    private final IdamService idamService;

    @Autowired
    public CreateGeneralLetterAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                  IdamService idamService) {
        super(mapper);
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (EventType.CREATE_GENERAL_LETTER.equals(eventType)
            || EventType.CREATE_GENERAL_LETTER_JUDGE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to clear general letter fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        FinremCaseData caseData = caseDetails.getData();

        GeneralLetterWrapper existingWrapper = caseData.getGeneralLetterWrapper();
        GeneralLetterWrapper wrapper = GeneralLetterWrapper.builder().build();

        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        addApplicantAndRespondentToDynamicList(dynamicListElements);
        if (existingWrapper != null) {
            wrapper.setGeneralLetterCollection(existingWrapper.getGeneralLetterCollection());
            wrapper.setGeneralLetterCreatedBy(idamService.getIdamFullName(userAuthorisation));
        }
        if (caseDetails.isContestedApplication()) {
            List<IntervenerWrapper> intvrWrappers = caseData.getInterveners();
            if (intvrWrappers != null && !intvrWrappers.isEmpty()) {
                intvrWrappers.forEach(intvrWrapper -> {
                    buildDynamicIntervenerList(dynamicListElements, intvrWrapper);
                });
            }
        }

        DynamicRadioList dynamicList = getDynamicRadioList(dynamicListElements);

        wrapper.setGeneralLetterAddressee(dynamicList);
        caseData.setGeneralLetterWrapper(wrapper);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    private void buildDynamicIntervenerList(List<DynamicRadioListElement> dynamicListElements, IntervenerWrapper intervenerWrapper) {
        if (intervenerWrapper.getIntervenerName() != null) {
            var label = intervenerWrapper.getIntervenerLabel();
            dynamicListElements.add(getDynamicRadioListElements(
                capitalize(intervenerWrapper.getIntervenerType().getTypeValue()), label));
        }
        if (intervenerWrapper.getIntervenerSolName() != null) {
            var label = intervenerWrapper.getIntervenerLabel() + " Solicitor";
            dynamicListElements.add(getDynamicRadioListElements(
                capitalize(intervenerWrapper.getIntervenerType().getTypeValue()) + "Solicitor", label));
        }
    }

    private void addApplicantAndRespondentToDynamicList(List<DynamicRadioListElement> listElements) {
        var roleLabels = List.of("Applicant", "Applicant Solicitor", "Respondent", "Respondent Solicitor", "Other");
        listElements.addAll(List.of(getDynamicRadioListElements(APPLICANT, roleLabels.get(0)),
            getDynamicRadioListElements(APPLICANT_SOLICITOR, roleLabels.get(1)),
            getDynamicRadioListElements(RESPONDENT, roleLabels.get(2)),
            getDynamicRadioListElements(RESPONDENT_SOLICITOR, roleLabels.get(3)),
            getDynamicRadioListElements(OTHER, roleLabels.get(4))));
    }
}
