package de.wr.mylibrary;

import de.wr.libdevutils.Debug;
import de.wr.libdevutils.DevUtils;
import de.wr.libdevutils.DebugOnly;

/**
 * Created by wolfgangreithmeier on 16.12.17.
 */

public class TestIgnore {
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

    @DebugOnly
    private void letItFailOnRelease() {
        System.out.print("Am i still alive?");
    }
}
