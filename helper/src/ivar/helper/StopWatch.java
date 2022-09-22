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
package ivar.helper;

public class StopWatch {

    private static final float MS_IN_SEC = 1000f;
    private static final float S_IN_MIN = 60f;
    private long start;
    private long stop;
    private long delta;
    private String timeTrace;
    private float deltaSec;
    private float deltaMin;

    public long start() {
        start = getTimeStamp();
        return start;
    }

    public long stop() {
        stop = getTimeStamp();
        delta = stop - start;
        deltaSec = delta / MS_IN_SEC;
        deltaMin = deltaSec / S_IN_MIN;
        timeTrace = "[TIME] " + delta + " ms, or " + deltaSec + " s, or " + deltaMin + " min.";
        return stop;
    }

    public String getIntermediateDeltaTimeTrace() {
        final long timestamp = getTimeStamp();
        long delta = timestamp - start;
        float deltaSec = delta / MS_IN_SEC;
        float deltaMin = deltaSec / S_IN_MIN;
        return "[TIME] " + delta + " ms, or " + deltaSec + " s, or " + deltaMin + " min.";
    }

    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

    public String getDeltaTimeTrace() {
        return timeTrace;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

    public long getDelta() {
        return delta;
    }

    public float getDeltaSec() {
        return deltaSec;
    }

    public float getDeltaMin() {
        return deltaMin;
    }
}
