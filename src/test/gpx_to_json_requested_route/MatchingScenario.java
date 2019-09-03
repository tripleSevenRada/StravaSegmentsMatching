package test.gpx_to_json_requested_route;

public enum MatchingScenario {
    // allow more "loose" matching for recorded tracks
    ROUTE("route"),
    RECORDED("recorded"),
    LOOSE("loose");
    public final String label;
    private MatchingScenario(String label) {
        this.label = label;
    }
}
