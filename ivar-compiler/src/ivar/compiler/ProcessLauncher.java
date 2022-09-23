/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.compiler;

import ivar.common.AbstractObject;
import ivar.helper.StopWatch;
import ivar.helper.io.IOHelper;
import java.io.*;

public class ProcessLauncher extends AbstractObject {

    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String[] ENVP = new String[]{"JAVA_HOME=" + JAVA_HOME};

    private class ProcessStreamFollower extends Thread {

        private InputStream inputStream;
        private String streamDesc;
        private boolean info;
        private boolean stop = false;
        private boolean stopped = false;

        public ProcessStreamFollower(final InputStream inputStream, final String streamDesc, final boolean isInfo) {
            this.inputStream = inputStream;
            this.streamDesc = streamDesc;
            info = isInfo;
        }

        public void end() {
            stop = true;
        }

        @Override
        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), IOHelper.BUFFER_SIZE);
            String line;
            try {
                while (!stop && (line = in.readLine()) != null) {
                    if (info) {
                        info(line);
                    } else {
                        error(line);
                    }
                }
            } catch (IOException e) {
                error("[ProcessStreamFollower] Error while reading " + streamDesc, e);
            } finally {
                debug("[ProcessStreamFollower] " + streamDesc + ". Closing stream ...");
                try {
                    if (in != null) {
                        in.close();
                        debug("[ProcessStreamFollower] " + streamDesc + ". After close().");
                        in = null;
                    }
                } catch (IOException e) {
                    // Nothing to do
                }
                debug("[ProcessStreamFollower] " + streamDesc + ". Closed");
            }
            stopped = true;
        }

        public boolean isStopped() {
            return stopped;
        }
    }

    public void run(final String commandLine, final String workingDirectory, final String[] systemProps) {
        beginBlock("[ProcessLauncher] start launching " + commandLine);
        final StopWatch sw = new StopWatch();
        sw.start();
        info("[ProcessLauncher] Working directory is " + workingDirectory);
        final Runtime runtime = Runtime.getRuntime();
        debug("[ProcessLauncher] JAVA_HOME = " + JAVA_HOME);
        final String[] envp = new String[systemProps.length + ENVP.length];
        int index = 0;

        for (final String sysprop : ENVP) {
            envp[index] = sysprop;
            index++;
        }
        for (final String sysprop : systemProps) {
            envp[index] = sysprop;
            index++;
        }

        Process process = null;
        ProcessStreamFollower inputStreamFollower = null;
        ProcessStreamFollower errorStreamFollower = null;

        try {
            try {
                process = runtime.exec(commandLine, envp, new File(workingDirectory));
                inputStreamFollower = new ProcessStreamFollower(process.getInputStream(), "process standard output stream", true);
                inputStreamFollower.start();
                errorStreamFollower = new ProcessStreamFollower(process.getErrorStream(), "process standard error stream ", false);
                errorStreamFollower.start();
                try {
                    final int exitValue = process.waitFor();
                    debug("[ProcessLauncher] Process waitFor, exit value is " + exitValue);
                } catch (InterruptedException e) {
                    // Nothing to do
                }
            } catch (IOException e) {
                error("[ProcessLauncher] IO error while launching " + commandLine + " in " + workingDirectory, e);
            }
        } finally {
            info("[ProcessLauncher] Process ended with return code : " + process.exitValue());
            if (inputStreamFollower != null) {
                inputStreamFollower.end();
            }
            if (errorStreamFollower != null) {
                errorStreamFollower.end();
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
        }

        while (inputStreamFollower != null && !inputStreamFollower.isStopped() && errorStreamFollower != null && !errorStreamFollower.isStopped()) {
            debug("[ProcessLauncher] waiting for error and input stream to close");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Nothing we can do
            }
        }
        inputStreamFollower = null;
        errorStreamFollower = null;

        sw.stop();
        endBlock("[ProcessLauncher] end " + sw.getDeltaTimeTrace());
    }
}
