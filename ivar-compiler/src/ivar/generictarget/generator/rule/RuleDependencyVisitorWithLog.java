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
package ivar.generictarget.generator.rule;

import ivar.common.logger.FullLogger;
import org.scriptonite.analysis.DepthFirstAdapter;

abstract class RuleDependencyVisitorWithLog extends DepthFirstAdapter {

    private FullLogger logger;

    public RuleDependencyVisitorWithLog(final FullLogger logger) {
        this.logger = logger;
    }

    public void beginBlock() {
        logger.beginBlock();
    }

    public void beginBlock(boolean ts) {
        logger.beginBlock(ts);
    }

    public void beginBlock(String message) {
        logger.beginBlock(message);
    }

    public void beginBlock(String message, boolean ts) {
        logger.beginBlock(message, ts);
    }

    public void beginCompileHelpBlock(String message) {
        logger.beginCompileHelpBlock(message);
    }

    public void endBlock() {
        logger.endBlock();
    }

    public void endBlock(boolean ts) {
        logger.endBlock(ts);
    }

    public void endBlock(String message) {
        logger.endBlock(message);
    }

    public void endBlock(String message, boolean ts) {
        logger.endBlock(message, ts);
    }

    public void endCompileHelpBlock() {
        logger.endCompileHelpBlock();
    }

    public void emptyLine() {
        logger.emptyLine();
    }

    public void error(Throwable t) {
        logger.error(t);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(Throwable t, boolean fatal) {
        logger.error(t, fatal);
    }

    public void error(String message, boolean fatal) {
        logger.error(message, fatal);
    }

    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public void error(String message, Throwable t, boolean fatal) {
        logger.error(message, t, fatal);
    }

    public void compileError(Throwable t) {
        logger.compileError(t);
    }

    public void compileError(String message) {
        logger.compileError(message);
    }

    public void compileError(Throwable t, boolean fatal) {
        logger.compileError(t, fatal);
    }

    public void compileError(String message, boolean fatal) {
        logger.compileError(message, fatal);
    }

    public void compileError(String message, Throwable t) {
        logger.compileError(message, t);
    }

    public void compileError(String message, Throwable t, boolean fatal) {
        logger.compileError(message, t, fatal);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void compileWarning(String message) {
        logger.compileWarning(message);
    }

    public void compileHelp(String message) {
        logger.compileHelp(message);
    }

    public void compileInfo(String message) {
        logger.compileInfo(message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void dumpException(Throwable t) {
        logger.dumpException(t);
    }
}
