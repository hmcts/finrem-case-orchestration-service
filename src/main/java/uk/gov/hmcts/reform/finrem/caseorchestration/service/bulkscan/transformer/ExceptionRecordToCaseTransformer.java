package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;

import java.util.Map;

public interface ExceptionRecordToCaseTransformer {
    Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord);
}
