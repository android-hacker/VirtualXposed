package com.lody.virtual.client.badger;

import android.content.Intent;

import com.lody.virtual.remote.BadgerInfo;

/**
 * @author Lody
 */
public interface IBadger {

    String getAction();

    BadgerInfo handleBadger(Intent intent);

}
