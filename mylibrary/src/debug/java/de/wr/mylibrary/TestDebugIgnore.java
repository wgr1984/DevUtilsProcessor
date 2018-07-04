package de.wr.mylibrary;

import de.wr.libdevutils.DebugOnly;

/**
 * TestDebugIgnore
 * <p>
 * created by Xiaoguang.Ren 04.07.2018
 */
public class TestDebugIgnore {

    @DebugOnly
    public void debugOnlyMethod() {
        System.out.println("am i still alive?");
    }
}
