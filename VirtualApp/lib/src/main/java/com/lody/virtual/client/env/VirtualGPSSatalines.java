package com.lody.virtual.client.env;

import java.util.ArrayList;
import java.util.List;

public class VirtualGPSSatalines {
    private static VirtualGPSSatalines INSTANCE;
    private int mAlmanacMask;
    private float[] mAzimuths;
    private float[] mElevations;
    private int mEphemerisMask;
    private float[] mSnrs;
    private int mUsedInFixMask;
    private int[] pnrs;
    private int[] prnWithFlags;
    private int svCount;

    static {
        INSTANCE = new VirtualGPSSatalines();
    }

    public int getAlmanacMask() {
        return this.mAlmanacMask;
    }

    public float[] getAzimuths() {
        return this.mAzimuths;
    }

    public float[] getElevations() {
        return this.mElevations;
    }

    public int getEphemerisMask() {
        return this.mEphemerisMask;
    }

    public int[] getPrns() {
        return this.pnrs;
    }

    public float[] getSnrs() {
        return this.mSnrs;
    }

    public int getUsedInFixMask() {
        return this.mUsedInFixMask;
    }

    public static VirtualGPSSatalines get() {
        return INSTANCE;
    }

    private VirtualGPSSatalines() {
        List<GPSStateline> statelines = new ArrayList<>();
        statelines.add(new GPSStateline(5, 1.0d, 5.0d, 112.0d, false, true, true));
        statelines.add(new GPSStateline(13, 13.5d, 23.0d, 53.0d, true, true, true));
        statelines.add(new GPSStateline(14, 19.1d, 6.0d, 247.0d, true, true, true));
        statelines.add(new GPSStateline(15, 31.0d, 58.0d, 45.0d, true, true, true));
        statelines.add(new GPSStateline(18, 0.0d, 52.0d, 309.0d, false, true, true));
        statelines.add(new GPSStateline(20, 30.1d, 54.0d, 105.0d, true, true, true));
        statelines.add(new GPSStateline(21, 33.2d, 56.0d, 251.0d, true, true, true));
        statelines.add(new GPSStateline(22, 0.0d, 14.0d, 299.0d, false, true, true));
        statelines.add(new GPSStateline(24, 25.9d, 57.0d, 157.0d, true, true, true));
        statelines.add(new GPSStateline(27, 18.0d, 3.0d, 309.0d, true, true, true));
        statelines.add(new GPSStateline(28, 18.2d, 3.0d, 42.0d, true, true, true));
        statelines.add(new GPSStateline(41, 28.8d, 0.0d, 0.0d, false, false, false));
        statelines.add(new GPSStateline(50, 29.2d, 0.0d, 0.0d, false, true, true));
        statelines.add(new GPSStateline(67, 14.4d, 2.0d, 92.0d, false, false, false));
        statelines.add(new GPSStateline(68, 21.2d, 45.0d, 60.0d, false, false, false));
        statelines.add(new GPSStateline(69, 17.5d, 50.0d, 330.0d, false, true, true));
        statelines.add(new GPSStateline(70, 22.4d, 7.0d, 291.0d, false, false, false));
        statelines.add(new GPSStateline(77, 23.8d, 10.0d, 23.0d, true, true, true));
        statelines.add(new GPSStateline(78, 18.0d, 47.0d, 70.0d, true, true, true));
        statelines.add(new GPSStateline(79, 22.8d, 41.0d, 142.0d, true, true, true));
        statelines.add(new GPSStateline(83, 0.2d, 9.0d, 212.0d, false, false, false));
        statelines.add(new GPSStateline(84, 16.7d, 30.0d, 264.0d, true, true, true));
        statelines.add(new GPSStateline(85, 12.1d, 20.0d, 317.0d, true, true, true));
        this.svCount = statelines.size();
        this.pnrs = new int[statelines.size()];
        for (int i = 0; i < statelines.size(); i++) {
            this.pnrs[i] = statelines.get(i).getPnr();
        }
        this.mSnrs = new float[statelines.size()];
        for (int i = 0; i < statelines.size(); i++) {
            this.mSnrs[i] = (float) statelines.get(i).getSnr();
        }
        this.mElevations = new float[statelines.size()];
        for (int i = 0; i < statelines.size(); i++) {
            this.mElevations[i] = (float) statelines.get(i).getElevation();
        }
        this.mAzimuths = new float[statelines.size()];
        for (int i = 0; i < statelines.size(); i++) {
            this.mAzimuths[i] = (float) statelines.get(i).getAzimuth();
        }
        this.mEphemerisMask = 0;
        for (int i = 0; i < statelines.size(); i++) {
            if (statelines.get(i).isHasEphemeris()) {
                this.mEphemerisMask |= 1 << (statelines.get(i).getPnr() - 1);
            }
        }
        this.mAlmanacMask = 0;
        for (int i = 0; i < statelines.size(); i++) {
            if (statelines.get(i).isHasAlmanac()) {
                this.mAlmanacMask |= 1 << (statelines.get(i).getPnr() - 1);
            }
        }
        this.mUsedInFixMask = 0;
        for (int i = 0; statelines.size() > i; i++) {
            if (statelines.get(i).isUseInFix()) {
                this.mUsedInFixMask |= 1 << (statelines.get(i).getPnr() - 1);
            }
        }
        this.prnWithFlags = new int[statelines.size()];
        for (int i = 0; i < statelines.size(); i++) {
            GPSStateline gpsStateline = statelines.get(i);
            this.prnWithFlags[i] =
                    (gpsStateline.isHasEphemeris() ? 1 : 0)
                            | (gpsStateline.isHasAlmanac() ? 1 : 0) << 1
                            | (gpsStateline.isUseInFix() ? 1 : 0) << 2
                            | 8
                            | (gpsStateline.getPnr() << 7);
        }
    }

    public int getSvCount() {
        return this.svCount;
    }

    public int[] getPrnWithFlags() {
        return this.prnWithFlags;
    }
}