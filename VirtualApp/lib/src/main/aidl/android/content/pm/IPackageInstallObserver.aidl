package android.content.pm;

/**
 * API for installation callbacks from the Package Manager.
 */
interface IPackageInstallObserver {
    void packageInstalled(in String packageName, int returnCode);
}

