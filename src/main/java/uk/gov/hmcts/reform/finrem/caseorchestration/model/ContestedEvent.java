package uk.gov.hmcts.reform.finrem.caseorchestration.model;

public enum ContestedEvent {

    ISSUE_APPLICATION("FR_issueApplication", "Issue Application", "Issue Application"),

    ALLOCATE_TO_JUDGE("FR_allocateToJudge", "Allocate to Judge", "Allocate to Judge");

    private final String id;
    private final String eventDescription;
    private final String eventSummary;

    ContestedEvent(String id, String eventDescription, String eventSummary) {
        this.id = id;
        this.eventDescription = eventDescription;
        this.eventSummary = eventSummary;
    }

    public String getId() {
        return id;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventSummary() {
        return eventSummary;
    }
}
