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
package ivar.common;

import ivar.common.logger.FullLogger;
import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;

public abstract class AbstractObject extends Object implements Logger {

    private transient FullLogger logger = null;

    public AbstractObject() {
        logger = LoggerDefaultImplementation.getInstance();
    }

    public boolean isDebug() {
        return true;
    }

    public void error(final String message) {
        logger.error(message);
    }

    public void error(final Throwable t) {
        logger.error(t);
    }

    public void error(final Throwable t, final boolean fatal) {
        logger.error(t, fatal);
    }

    public void error(final String message, final boolean fatal) {
        logger.error(message, fatal);
    }

    public void warning(final String message) {
        logger.warning(message);
    }

    public void info(final String message) {
        logger.info(message);
    }

    public void debug(final String message) {
        if (isDebug()) {
            logger.debug(message);
        }
    }

    public void error(final String message, final Throwable t) {
        logger.error(message, t);
    }

    public void error(final String message, final Throwable t, final boolean fatal) {
        logger.error(message, t, fatal);
    }

    public void beginBlock() {
        logger.beginBlock();
    }

    public void beginBlock(final String message) {
        logger.beginBlock(message);
    }

    public void beginBlock(final String message, final boolean timeStamp) {
        logger.beginBlock(message, timeStamp);
    }

    public void endBlock() {
        logger.endBlock();
    }

    public void endBlock(final String message) {
        logger.endBlock(message);
    }

    public void endBlock(final String message, final boolean timeStamp) {
        logger.endBlock(message, timeStamp);
    }

    public FullLogger getLogger() {
        return logger;
    }

    public void compileError(final Throwable t) {
        logger.compileError(t);
    }

    public void compileError(final String message) {
        logger.compileError(message);
    }

    public void compileError(final Throwable t, final boolean fatal) {
        logger.compileError(t, fatal);
    }

    public void compileError(final String message, final boolean fatal) {
        logger.compileError(message, fatal);
    }

    public void compileError(final String message, final Throwable t) {
        logger.compileError(message, t);
    }

    public void compileError(final String message, final Throwable t, final boolean fatal) {
        logger.compileError(message, t, fatal);
    }

    public void compileWarning(final String message) {
        logger.compileWarning(message);
    }

    public void compileInfo(final String message) {
        logger.compileInfo(message);
    }

    public void compileHelp(final String message) {
        logger.compileHelp(message);
    }

    public void beginCompileHelpBlock(final String message) {
        logger.beginCompileHelpBlock(message);
    }

    public void endCompileHelpBlock() {
        logger.endCompileHelpBlock();
    }

    public void emptyLine() {
        logger.emptyLine();
    }
}
