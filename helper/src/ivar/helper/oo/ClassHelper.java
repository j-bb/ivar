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
package ivar.helper.oo;

import java.lang.reflect.Method;

public final class ClassHelper {

    private ClassHelper() {
    }

    public static String getShortName(final Object o) {
        return getShortName(o.getClass());
    }

    public static String getShortName(final Class c) {
        String result = null;
        final String className = c.getName();
        final int dotIndex = className.lastIndexOf(".");
        if (dotIndex > 0) {
            result = className.substring(dotIndex + 1, className.length());
        } else {
            result = className;
        }
        return result;
    }

    public static Method getSetter(final Method[] methods, final String paramName, final boolean isColection) {
        Method result = null;
        for (final Method method : methods) {
            final String methodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final String setterName = methodName.toLowerCase().substring(3, methodName.length());
            final boolean isSetter = methodName.toLowerCase().startsWith("set");
            final boolean isSetters = methodName.toLowerCase().startsWith("add");
            final boolean isNameOK = setterName.equals(paramName.toLowerCase());
            final boolean isNamesOK = setterName.equals(paramName.toLowerCase());
            if ((isSetter && isNameOK) || (isSetters && isNamesOK)) {
                if (parameterTypes.length == 1 && parameterTypes[0] == String.class) {
                    result = method;
                    break;
                }
            }
        }
        return result;
    }
}
