/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.app;

import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.PendingTransactionActions;
import android.app.servertransaction.TransactionExecutor;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.MergedConfiguration;
import android.view.DisplayAdjustments;

import java.util.List;
import java.util.Map;

import mirror.com.android.internal.content.ReferrerIntent;

/**
 * Defines operations that a {@link ClientTransaction} or its items
 * can perform on client.
 * @hide
 */
public abstract class ClientTransactionHandler {

    // Schedule phase related logic and handlers.
    /** Prepare and schedule transaction for execution. */
    void scheduleTransaction(ClientTransaction transaction) {

    }
    /**
     * Execute transaction immediately without scheduling it. This is used for local requests, so
     * it will also recycle the transaction.
     */
    public void executeTransaction(ClientTransaction transaction) {
    }

    /**
     * Get the {@link TransactionExecutor} that will be performing lifecycle transitions and
     * callbacks for activities.
     */
    abstract TransactionExecutor getTransactionExecutor();
    abstract void sendMessage(int what, Object obj);
    // Prepare phase related logic and handlers. Methods that inform about about pending changes or
    // do other internal bookkeeping.
    /** Set pending config in case it will be updated by other transaction item. */
    public abstract void updatePendingConfiguration(Configuration config);
    /** Set current process state. */
    public abstract void updateProcessState(int processState, boolean fromIpc);
    // Execute phase related logic and handlers. Methods here execute actual lifecycle transactions
    // and deliver callbacks.
    /** Destroy the activity. */
    public abstract void handleDestroyActivity(IBinder token, boolean finishing, int configChanges,
            boolean getNonConfigInstance, String reason);
    /** Pause the activity. */
    public abstract void handlePauseActivity(IBinder token, boolean finished, boolean userLeaving,
            int configChanges, PendingTransactionActions pendingActions, String reason);

    // Android 12
    /** Destroy the activity. */
    public abstract void handleDestroyActivity(ActivityThread.ActivityClientRecord r, boolean finishing,
                                               int configChanges, boolean getNonConfigInstance, String reason);

    /** Pause the activity. */
    public abstract void handlePauseActivity(ActivityThread.ActivityClientRecord r, boolean finished, boolean userLeaving,
                                             int configChanges, PendingTransactionActions pendingActions, String reason);
    /**
     * Resume the activity.
     * @param token Target activity token.
     * @param finalStateRequest Flag indicating if this call is handling final lifecycle state
     *                          request for a transaction.
     * @param isForward Flag indicating if next transition is forward.
     * @param reason Reason for performing this operation.
     */
    public abstract void handleResumeActivity(IBinder token, boolean finalStateRequest,
            boolean isForward, String reason);

    // Android 12
    public abstract void handleResumeActivity(ActivityThread.ActivityClientRecord record, boolean finalStateRequest,
                                              boolean isForward, String reason);

    /**
     * Stop the activity.
     * @param token Target activity token.
     * @param show Flag indicating whether activity is still shown.
     * @param configChanges Activity configuration changes.
     * @param pendingActions Pending actions to be used on this or later stages of activity
     *                       transaction.
     * @param finalStateRequest Flag indicating if this call is handling final lifecycle state
     *                          request for a transaction.
     * @param reason Reason for performing this operation.
     */
    public abstract void handleStopActivity(IBinder token, boolean show, int configChanges,
            PendingTransactionActions pendingActions, boolean finalStateRequest, String reason);

    // Android 11
    public abstract void handleStopActivity(IBinder token, int configChanges,
                                            PendingTransactionActions pendingActions, boolean finalStateRequest, String reason);

    // Android 12
    public abstract void handleStopActivity(ActivityThread.ActivityClientRecord r, int configChanges,
                                            PendingTransactionActions pendingActions, boolean finalStateRequest, String reason);

    /** Report that activity was stopped to server. */
    public abstract void reportStop(PendingTransactionActions pendingActions);
    /** Restart the activity after it was stopped. */
    public abstract void performRestartActivity(IBinder token, boolean start);
    /** Restart the activity after it was stopped. */
    public abstract void performRestartActivity(ActivityThread.ActivityClientRecord r, boolean start);

    /** Deliver activity (override) configuration change. */
    public abstract void handleActivityConfigurationChanged(IBinder activityToken,
            Configuration overrideConfig, int displayId);
    public abstract void handleActivityConfigurationChanged(ActivityThread.ActivityClientRecord r,
                                                            Configuration overrideConfig, int displayId);

    /** Deliver result from another activity. */
    public abstract void handleSendResult(IBinder token, List results, String reason);

    /** Deliver result from another activity. */
    public abstract void handleSendResult(
            ActivityThread.ActivityClientRecord r, List results, String reason);

    /** Deliver multi-window mode change notification. */
    public abstract void handleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode,
            Configuration overrideConfig);
    /** Deliver new intent. */
    public abstract void handleNewIntent(IBinder token, List intents,
            boolean andPause);
    public abstract void handleNewIntent(
            ActivityThread.ActivityClientRecord r, List<ReferrerIntent> intents);
    /** Deliver picture-in-picture mode change notification. */
    public abstract void handlePictureInPictureModeChanged(IBinder token, boolean isInPipMode,
            Configuration overrideConfig);

    // Android 11
    public abstract void handlePictureInPictureRequested(IBinder token);
    public abstract void handlePictureInPictureRequested(ActivityThread.ActivityClientRecord r);

    /** Signal to an activity (that is currently in PiP) of PiP state changes. */
    public abstract void handlePictureInPictureStateChanged(ActivityThread.ActivityClientRecord r,
                                                            Parcelable pipState);

    /** Whether the activity want to handle splash screen exit animation */
    public abstract boolean isHandleSplashScreenExit(IBinder token);

    /** Attach a splash screen window view to the top of the activity */
    public abstract void handleAttachSplashScreenView(ActivityThread.ActivityClientRecord r,
                                                      Parcelable parcelable);

    /** Hand over the splash screen window view to the activity */
    public abstract void handOverSplashScreenView(ActivityThread.ActivityClientRecord r);

    /** Update window visibility. */
    public abstract void handleWindowVisibility(IBinder token, boolean show);
    /** Perform activity launch. */
    public abstract Activity handleLaunchActivity(ActivityThread.ActivityClientRecord r,
            PendingTransactionActions pendingActions, Intent customIntent);
    /** Perform activity start. */
    public abstract void handleStartActivity(ActivityThread.ActivityClientRecord r,
            PendingTransactionActions pendingActions);

    // Android 12
    /** Perform activity start. */
    public abstract void handleStartActivity(ActivityThread.ActivityClientRecord r,
                                             PendingTransactionActions pendingActions, ActivityOptions options);

    // Android 11
    public abstract void handleStartActivity(IBinder binder,
                                             PendingTransactionActions pendingActions);
    /** Get package info. */
    public abstract LoadedApk getPackageInfoNoCheck(ApplicationInfo ai,
                                                    CompatibilityInfo compatInfo);
    /** Deliver app configuration change notification. */
    public abstract void handleConfigurationChanged(Configuration config);

    public abstract void handleFixedRotationAdjustments(IBinder token,
                                                        DisplayAdjustments.FixedRotationAdjustments fixedRotationAdjustments);

    /**
     * Add {@link ActivityThread.ActivityClientRecord} that is preparing to be launched.
     * @param token Activity token.
     * @param activity An initialized instance of {@link ActivityThread.ActivityClientRecord} to use during launch.
     */
    public abstract void addLaunchingActivity(IBinder token, ActivityThread.ActivityClientRecord activity);

    /**
     * Get {@link ActivityThread.ActivityClientRecord} that is preparing to be launched.
     * @param token Activity token.
     * @return An initialized instance of {@link ActivityThread.ActivityClientRecord} to use during launch.
     */
    public abstract ActivityThread.ActivityClientRecord getLaunchingActivity(IBinder token);

    /**
     * Remove {@link ActivityThread.ActivityClientRecord} from the launching activity list.
     * @param token Activity token.
     */
    public abstract void removeLaunchingActivity(IBinder token);

    /**
     * Get {@link ActivityThread.ActivityClientRecord} instance that corresponds to the
     * provided token.
     */
    public abstract ActivityThread.ActivityClientRecord getActivityClient(IBinder token);
    /**
     * Prepare activity relaunch to update internal bookkeeping. This is used to track multiple
     * relaunch and config update requests.
     * @param token Activity token.
     * @param pendingResults Activity results to be delivered.
     * @param pendingNewIntents New intent messages to be delivered.
     * @param configChanges Mask of configuration changes that have occurred.
     * @param config New configuration applied to the activity.
     * @param preserveWindow Whether the activity should try to reuse the window it created,
     *                        including the decor view after the relaunch.
     * @return An initialized instance of {@link ActivityThread.ActivityClientRecord} to use during
     *         relaunch, or {@code null} if relaunch cancelled.
     */
    public abstract ActivityThread.ActivityClientRecord prepareRelaunchActivity(IBinder token,
            List pendingResults, List pendingNewIntents,
            int configChanges, MergedConfiguration config, boolean preserveWindow);
    /**
     * Perform activity relaunch.
     * @param r Activity client record prepared for relaunch.
     * @param pendingActions Pending actions to be used on later stages of activity transaction.
     * */
    public abstract void handleRelaunchActivity(ActivityThread.ActivityClientRecord r,
            PendingTransactionActions pendingActions);
    /**
     * Report that relaunch request was handled.
     * @param token Target activity token.
     * @param pendingActions Pending actions initialized on earlier stages of activity transaction.
     *                       Used to check if we should report relaunch to WM.
     * */
    public abstract void reportRelaunch(IBinder token, PendingTransactionActions pendingActions);

    public abstract Map getActivitiesToBeDestroyed();

    // Android 10
    public abstract Activity getActivity(IBinder token);

    public abstract void updatePendingActivityConfiguration(IBinder arg1, Configuration arg2);

    public abstract void handleTopResumedActivityChanged(IBinder arg1, boolean arg2, String arg3);

    public abstract void handleTopResumedActivityChanged(ActivityThread.ActivityClientRecord record, boolean arg2, String arg3);


    /** Count how many activities are launching. */
    public abstract void countLaunchingActivities(int num);

    /** Deliver new intent. */
    public abstract void handleNewIntent(IBinder token, List intents);
}