package android.content.pm;

/**
 * API for package data change related callbacks from the Package Manager.
 * Some usage scenarios include deletion of cache directory, generate
 * statistics related to code, data, cache usage(TODO)
 */
interface IPackageDataObserver {
    void onRemoveCompleted(in String packageName, boolean succeeded);
}
