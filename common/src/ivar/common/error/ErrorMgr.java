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
package ivar.common.error;

import java.util.Iterator;
import java.util.Stack;

public class ErrorMgr {

    private Stack<Error> errors = new Stack<Error>();

    public ErrorMgr() {
    }

    private boolean alreadyExist(final String errorMessage) {
        boolean result = false;
        for (final Error error : errors) {
            if (error.getMessage().equals(errorMessage)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void clear() {
        errors.clear();
    }

    public void addError(final Error error) {
        if (!alreadyExist(error.getMessage())) {
            errors.push(error);
        }
    }

    public void addError(final String message) {
        if (!alreadyExist(message)) {
            errors.push(new Error(message));
        }
    }

    public void addError(final String message, final boolean fatal) {
        if (!alreadyExist(message)) {
            errors.push(new Error(message, fatal));
        }
    }

    public Iterator<Error> getErrors() {
        return errors.iterator();
    }
}
