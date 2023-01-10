package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

public interface Deserializer<T> {

    T deserialize(String source);
}
