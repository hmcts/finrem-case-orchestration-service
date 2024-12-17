package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;

/**
 * Interface representing a container for an {@link Approvable} object.
 */
public interface HasApprovable {
    Approvable getValue();
}
