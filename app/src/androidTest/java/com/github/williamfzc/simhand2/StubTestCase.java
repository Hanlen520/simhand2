/*
MIT License

Copyright (c) 2018 William Feng

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.github.williamfzc.simhand2;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class StubTestCase {
    private static final String BASE_PACKAGE_NAME = "com.github.williamfzc.simhand2";
    private static final String FIRST_PAGE = "MainActivity";
    private static final String TAG = "StubTestCase";
    private int serverPort;
    private String parentIP;
    private UiDevice mDevice;
    private APIServer mServer;

    private boolean runServer(UiDevice targetDevice) {
        try {
            mServer = new APIServer(serverPort, targetDevice);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Before
    public void initDevice() {
        serverPort = Integer.valueOf(SHInstrumentationTestRunner.getPort());
        parentIP = SHInstrumentationTestRunner.getParentIP();

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            // ignore and record
            e.printStackTrace();
        }

        // grant permission ( > 6.0
        try {
            mDevice.executeShellCommand("pm grant " + BASE_PACKAGE_NAME + " android.permission.READ_EXTERNAL_STORAGE");
            mDevice.executeShellCommand("pm grant " + BASE_PACKAGE_NAME + " android.permission.WRITE_EXTERNAL_STORAGE");
        } catch (IOException e) {
            // ignore and record
            e.printStackTrace();
        }

        // Start main page
        launchFront();
        mDevice.pressHome();

        // startup server
        if (runServer(mDevice)) {
            Log.i(TAG, "simhand already started on " + serverPort);
        } else {
            Log.e(TAG, "simhand start failed");
            throw new RuntimeException("simhand start up failed");
        }
    }

    @Test
    @LargeTest
    public void KeepAlive() throws InterruptedException {
        while (true) {
            Log.i(TAG, "simhand is alive :)");
            Thread.sleep(5000);
        }
    }

    @After
    public void stopServer() {
        if (mServer != null) {
            mServer.stop();
        }
        mDevice = null;
    }

    private void launchFront() {
        launchPackage(BASE_PACKAGE_NAME, FIRST_PAGE);
    }

    private void launchPackage(String targetPackage, String targetActivity) {
        // todo: why the way uses intent didn't work???
        try {
            mDevice.executeShellCommand("am start -n " + targetPackage + "/." + targetActivity);
        } catch (IOException e) {
            // ignore
            e.printStackTrace();
        }
    }
}