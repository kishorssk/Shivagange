package com.srirangadigital.tungalahari;

import java.io.IOException;

/**
 * Created by root on 23/4/17.
 */

public class Common {

    public boolean isConnected() throws InterruptedException, IOException {

        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec (command).waitFor() == 0);
    }
}
