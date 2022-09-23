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
package ivar.common.logger;

public interface Logger {

    void error(final String message);

    void error(final Throwable t);

    void error(final Throwable t, final boolean fatal);

    void error(final String message, final boolean fatal);

    void warning(final String message);

    void info(final String message);

    void debug(final String message);

    void error(final String message, final Throwable t);

    void error(final String message, final Throwable t, final boolean fatal);

    void beginBlock();

    void beginBlock(final String message);

    void beginBlock(final String message, final boolean timeStamp);

    void endBlock();

    void endBlock(final String message);

    void endBlock(final String message, final boolean timeStamp);

    void compileError(final Throwable t);

    void compileError(final String message);

    void compileError(final Throwable t, final boolean fatal);

    void compileError(final String message, final boolean fatal);

    void compileError(final String message, final Throwable t);

    void compileError(final String message, final Throwable t, final boolean fatal);

    void compileWarning(final String message);

    void compileInfo(final String message);

    void compileHelp(final String message);

    void beginCompileHelpBlock(final String message);

    void endCompileHelpBlock();

    void emptyLine();
}
