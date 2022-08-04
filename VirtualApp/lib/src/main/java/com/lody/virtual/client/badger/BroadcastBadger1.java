package com.lody.virtual.client.badger;

import android.content.Intent;

import com.lody.virtual.remote.BadgerInfo;

/**
 * @author Lody
 */
public abstract class BroadcastBadger1 implements IBadger {

    public abstract String getAction();

    public abstract String getPackageKey();

    public abstract String getClassNameKey();

    public abstract String getCountKey();

    @Override
    public BadgerInfo handleBadger(Intent intent) {
        BadgerInfo info = new BadgerInfo();
        info.packageName = intent.getStringExtra(getPackageKey());
        if (getClassNameKey() != null) {
            info.className = intent.getStringExtra(getClassNameKey());
        }
        info.badgerCount = intent.getIntExtra(getCountKey(), 0);
        return info;
    }


    static class LGHomeBadger extends BroadcastBadger1 {

        @Override
        public String getAction() {
            return "android.intent.action.BADGE_COUNT_UPDATE";
        }

        @Override
        public String getPackageKey() {
            return "badge_count_package_name";
        }

        @Override
        public String getClassNameKey() {
            return "badge_count_class_name";
        }

        @Override
        public String getCountKey() {
            return "badge_count";
        }
    }

    static class AdwHomeBadger extends BroadcastBadger1 {

        @Override
        public String getAction() {
            return "org.adw.launcher.counter.SEND";
        }

        @Override
        public String getPackageKey() {
            return "PNAME";
        }

        @Override
        public String getClassNameKey() {
            return "CNAME";
        }

        @Override
        public String getCountKey() {
            return "COUNT";
        }
    }

    static class AospHomeBadger extends BroadcastBadger1 {

        @Override
        public String getAction() {
            return "android.intent.action.BADGE_COUNT_UPDATE";
        }

        @Override
        public String getPackageKey() {
            return "badge_count_package_name";
        }

        @Override
        public String getClassNameKey() {
            return "badge_count_class_name";
        }

        @Override
        public String getCountKey() {
            return "badge_count";
        }
    }


    static class NewHtcHomeBadger2 extends BroadcastBadger1 {

        @Override
        public String getAction() {
            return "com.htc.launcher.action.UPDATE_SHORTCUT";
        }

        @Override
        public String getPackageKey() {
            return "packagename";
        }

        @Override
        public String getClassNameKey() {
            return null;
        }

        @Override
        public String getCountKey() {
            return "count";
        }
    }


    static class OPPOHomeBader extends BroadcastBadger1 {

        @Override
        public String getAction() {
            return "com.oppo.unsettledevent";
        }

        @Override
        public String getPackageKey() {
            return "pakeageName";
        }

        @Override
        public String getClassNameKey() {
            return null;
        }

        @Override
        public String getCountKey() {
            return "number";
        }
    }



}
