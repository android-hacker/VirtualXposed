package android.content.pm;

interface IPackageInstallerCallback {
    void onSessionCreated(int sessionId);
    void onSessionBadgingChanged(int sessionId);
    void onSessionActiveChanged(int sessionId, boolean active);
    void onSessionProgressChanged(int sessionId, float progress);
    void onSessionFinished(int sessionId, boolean success);
}
