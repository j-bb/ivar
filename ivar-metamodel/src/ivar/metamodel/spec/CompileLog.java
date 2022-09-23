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
package ivar.metamodel.spec;

import ivar.metamodel.IdentifiableObject;
import ivar.metamodel.IvarCloneable;
import java.util.Date;
import java.util.Map;

public class CompileLog extends IdentifiableObject {

    private Date uniqueCompileDate;
    private boolean compileResult;
    private String compileOptions;
    private String outputFolder;
    private Integer scenarioCount;
    private Integer jumpCount;
    private Integer stepCount;
    private Integer ruleCount;
    private Integer filterCount;
    private Integer lineCount;
    private Long duration;

    public CompileLog() {
        super();
    }

    public CompileLog(Date ucDate, String options, String outputFolder) {
        uniqueCompileDate = ucDate;
        compileOptions = options;
        this.outputFolder = outputFolder;
    }

    @Override
    public IvarCloneable getJPAClone() {
        return getJPAClone(new CompileLog(uniqueCompileDate, compileOptions, outputFolder));
    }

    protected IvarCloneable getJPAClone(CompileLog log) {
        if (uniqueCompileDate != null) {
            log.uniqueCompileDate = (Date) uniqueCompileDate.clone();
        }

        if (compileOptions != null) {
            log.compileOptions = new String(compileOptions);
        }

        if (outputFolder != null) {
            log.outputFolder = new String(outputFolder);
        }

        log.compileResult = compileResult;

        if (jumpCount != null) {
            log.jumpCount = new Integer(jumpCount);
        }

        if (scenarioCount != null) {
            log.scenarioCount = new Integer(scenarioCount);
        }

        if (stepCount != null) {
            log.stepCount = new Integer(stepCount);
        }

        if (ruleCount != null) {
            log.ruleCount = new Integer(ruleCount);
        }

        if (lineCount != null) {
            log.lineCount = new Integer(lineCount);
        }

        if (filterCount != null) {
            log.filterCount = new Integer(filterCount);
        }

        if (duration != null) {
            log.duration = new Long(duration);
        }

        return log;
    }

    public Date getUniqueCompileDate() {
        return uniqueCompileDate;
    }

    public boolean isCompileResult() {
        return compileResult;
    }

    public String getCompileOptions() {
        return compileOptions;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public Long getDuration() {
        return duration;
    }

    public void setUniqueCompileDate(final Date date) {
        this.uniqueCompileDate = date;
    }

    public void setCompileResult(final boolean compileResult) {
        this.compileResult = compileResult;
    }

    public void setCompileOptions(final String compileOptions) {
        this.compileOptions = compileOptions;
    }

    public void setOutputFolder(final String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setApplicationMetrics(final Map<String, Integer> applicationMetrics) {
        this.scenarioCount = applicationMetrics.get("scenarioNumber");
        this.stepCount = applicationMetrics.get("stepNumber");
        this.jumpCount = applicationMetrics.get("jumpNumber");
        this.lineCount = applicationMetrics.get("lineNumber");
        this.ruleCount = applicationMetrics.get("ruleNumber");
        this.filterCount = applicationMetrics.get("filterNumber");
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }
}
