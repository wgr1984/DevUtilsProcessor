package de.wr.mylibrary2;

import de.wr.libdevutils.Debug;
import de.wr.libdevutils.DevUtils;

/**
 * Created by wolfgangreithmeier on 16.12.17.
 */

public class TestIgnore2 {
    public void printoutTest() {
        if (DevUtils.IS_DEBUG) {
            printoutTestDebug();
        } else {
            printoutTestRelease();
        }
    }

    @Debug
    private void printoutTestDebug() {
        System.out.println("Test Debug");
    }

    private void printoutTestRelease() {
        System.out.println("Test Release");
    }
}
