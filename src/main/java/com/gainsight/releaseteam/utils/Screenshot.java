package com.gainsight.releaseteam.utils;

import com.gainsight.pageobject.core.fluent.FluentDriver;

/**
 * Created by Abhilash Thaduka on 4/8/2017.
 */
public class Screenshot {

    public static byte[] takeScreenShot(FluentDriver fluentDriver) {
        if (fluentDriver != null)
            return fluentDriver.takeScreenShot();
        else
            return new byte[2];
    }
}


