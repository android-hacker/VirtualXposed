package com.lody.virtual.server.log;

/**
 * @author Lody
 */

public interface ILogAgent {

    void reportLog(int level, String tag, String msg);
}
