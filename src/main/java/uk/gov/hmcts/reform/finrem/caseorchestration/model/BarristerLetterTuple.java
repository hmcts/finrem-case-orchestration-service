package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;

@Data
public class BarristerLetterTuple {

    private DocumentHelper.PaperNotificationRecipient recipient;
    private String authToken;
    private BarristerChangeType changeType;

    public BarristerLetterTuple(DocumentHelper.PaperNotificationRecipient recipient,
                                String authToken,
                                BarristerChangeType changeType) {
        this.recipient = recipient;
        this.authToken = authToken;
        this.changeType = changeType;
    }

    public static BarristerLetterTuple of(DocumentHelper.PaperNotificationRecipient recipient,
                                          String authToken,
                                          BarristerChangeType changeType) {
        return new BarristerLetterTuple(recipient, authToken, changeType);
    }

    public static BarristerLetterTuple of(Pair<String, BarristerChangeType> pair,
                                          DocumentHelper.PaperNotificationRecipient recipient) {
        return new BarristerLetterTuple(recipient, pair.getLeft(), pair.getRight());
    }
}
