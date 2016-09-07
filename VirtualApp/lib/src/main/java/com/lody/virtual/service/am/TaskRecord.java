package com.lody.virtual.service.am;

import android.content.Intent;

import java.util.ArrayList;

/**
 * @author Lody
 */

public class TaskRecord {
    public int taskId;
    public int userId;
    public String affinity;
    public Intent taskRoot;
    public final ArrayList<ActivityRecord> activities = new ArrayList<>();

    public TaskRecord(int taskId, int userId, String affinity, Intent intent) {
        this.taskId = taskId;
        this.userId = userId;
        this.affinity = affinity;
        this.taskRoot = intent;
    }
}
