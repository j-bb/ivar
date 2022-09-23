/*
    Java JSON RPC
    RPC Java POJO

    This library is dual-licensed under the GNU Lesser General Public License (LGPL) and the Eclipse Public License (EPL).
    Check http://qooxdoo.org/license

    This library is also licensed under the Apache license.
    Check http://www.apache.org/licenses/LICENSE-2.0

    Contribution:
    This contribution is provided by Jean-Baptiste Briaud
 */

package lib.rpc;

import lib.serialization.JavaSerializer;
import lib.serialization.UnserializationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteCallUtils {
    protected JavaSerializer serializer;

    public RemoteCallUtils(final JavaSerializer javaSerializer) {
        this.serializer = javaSerializer;
    }

    public Remote getServiceInstance(final String serviceClassName, Class requiredClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (requiredClass == null) {
            requiredClass = java.rmi.Remote.class;
        }
        System.out.println("2020 DBG. RemoteCallUtils.getServiceInstance(" + serviceClassName + ", " + serviceClassName == null ? "null" : serviceClassName.toString());
        final Class serviceClass = Class.forName(serviceClassName);
        if (!requiredClass.isAssignableFrom(serviceClass)) {
            throw new ClassCastException("The requested service class " + serviceClassName + " is not from the required type " + requiredClass.getName());
        }

        return (Remote) serviceClass.newInstance();
    }


    /**
     * Invokes a method compatible to the specified parameters.
     *
     * @param instance          the object on which to invoke the
     *                          method (must not be
     *                          <code>null</code>).
     * @param methodName        the name of the method to invoke (must
     *                          not be <code>null</code>).
     * @param originalArguments the method parameters (as JSON
     *                          objects - must not be
     *                          <code>null</code>).
     */
    public Object callCompatibleMethod(final Object instance, final String methodName, final List<Object> originalArguments, final Object... extraArgs) throws RemoteException, InvocationTargetException, IllegalAccessException, RpcException, NoSuchMethodException {
        int extraArgsCount = 0;
        if (extraArgs != null && extraArgs.length > 0) {
            extraArgsCount = extraArgs.length;
        }
        final int argsCount = extraArgsCount + originalArguments.size();

        final Class clazz = instance.getClass();
        Method methodToCall = null;
        Class[] methodParameterTypes;

        List<Object> convertedArguments = null;

        // Search for the appropriate method among all public methods of the instance
        final Method[] methods = clazz.getMethods();

        boolean goodMethodNameFound = false;
        Class[] badExceptions = null;
        Exception argsConversionException = null;

        for (final Method candidateMethod : methods) {
            if (!candidateMethod.getName().equals(methodName)) {
                continue;
            }
            goodMethodNameFound = true;

            methodParameterTypes = candidateMethod.getParameterTypes();
            if (methodParameterTypes.length != argsCount) {
                continue;
            }

            Class[] exceptions = candidateMethod.getExceptionTypes();
            if (!(exceptions.length == 1 && exceptions[0].isAssignableFrom(RemoteException.class))) {
                badExceptions = exceptions;
                continue;
            }

            // Check that extra arguments have the good type
            if (!checkExtraArgs(candidateMethod, extraArgs)) {
                // Arguments do not match, try next method
                continue;
            }

            // try to convert the serialized arguments
            try {
                convertedArguments = unserializeArguments(originalArguments, candidateMethod, extraArgsCount);
            } catch (UnserializationException e) {
                // Arguments do not match, try next method
                argsConversionException = e;
                continue;
            } catch (InstantiationException e) {
                // Arguments do not match, try next method
                argsConversionException = e;
                continue;
            } catch (IllegalAccessException e) {
                // Arguments do not match, try next method
                argsConversionException = e;
                continue;
            }

            // All tests passed (method name + arguments types), we've found our method!
            methodToCall = candidateMethod;
            break;
        }

        if (methodToCall == null) {
            // No appropriate method has been found
            if (goodMethodNameFound) {
                if (argsConversionException != null) {
                    throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.PARAMETER_MISMATCH, "Bad arguments for method " + methodName, argsConversionException);
                } else if (badExceptions != null) {
                    throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.PARAMETER_MISMATCH, "The invoked method " + methodName + " must throw a java.rmi.Exception and only a java.rmi.RemoteException. " + getDeclaredExceptionsMessage(badExceptions));
                } else {
                    throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.PARAMETER_MISMATCH, "Bad arguments count (" + (argsCount) + ") for method " + methodName + ".");
                }
            } else {
                throw new NoSuchMethodException(methodName + "(...)");
            }
        }

        // Construct array of all arguments from extra arguments (1st position) and arguments obtained from serialization (2nd position)
        final List<Object> arguments = new ArrayList<Object>(argsCount);
        arguments.addAll(Arrays.asList(extraArgs));
        arguments.addAll(convertedArguments);

        // Invoke method
        Object methodResult = null;
        try {
            methodResult = methodToCall.invoke(instance, arguments.toArray());
        } catch (IllegalAccessException e) {
            System.err.println("ERROR IllegalAccessException while trying to call " + methodToCall.getName() + " on a " + instance.getClass().getName() + " with arguments : (");
            for (final Object arg : arguments) {
                System.err.println("   " + arg.getClass().getName() + " : " + arg + ", ");
            }
            System.err.println(")");
            trace(e);
            throw e;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RemoteException) {
                throw (RemoteException) e.getCause();
            } else {
                // Unwanted exception, like NullPointerException
                // TODO: raise a different type of exception
                trace(e);
                throw e;
            }
        }

        return methodResult;
    }


    protected List<Object> unserializeArguments(final List<Object> src, Method method, int startIndex) throws UnserializationException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final List<Object> unserializedArgs = new ArrayList<Object>();

        final Class[] argsClasses = method.getParameterTypes();
        final Type[] argsTypes = method.getGenericParameterTypes();

        /* Number of arguments should have been tested before (inside callCompatibleMethod) so we should not worry about that */

        for (int i = startIndex; i < argsClasses.length; i++) {
            final Object arg = serializer.unserialize(src.get(i - startIndex), argsClasses[i], argsTypes[i]);
            unserializedArgs.add(arg);
        }

        return unserializedArgs;
    }

    protected boolean checkExtraArgs(final Method method, final Object... extraArgs) {
        boolean areArgsCompatible = true;

        if (extraArgs != null && extraArgs.length > 0) {
            final Class[] argsClasses = method.getParameterTypes();
            for (int i = 0; i < extraArgs.length; i++) {
                if (extraArgs[i] != null && !(argsClasses[i].isAssignableFrom(extraArgs[i].getClass()))) {
                    areArgsCompatible = false;
                    break;
                }
            }
        }

        return areArgsCompatible;
    }


    protected String getDeclaredExceptionsMessage(Class[] exceptions) {
        StringBuilder builder = new StringBuilder();

        if (exceptions == null || exceptions.length < 1) {
            builder.append("No exception is thrown by the declared method.");
        } else {
            builder.append("Declared exceptions of the method are: ");
            for (int i = 0; i < exceptions.length - 1; i++) {
                builder.append(exceptions[i]);
                builder.append(", ");
            }
            builder.append(exceptions[exceptions.length - 1]);
            builder.append('.');
        }

        return builder.toString();
    }

    protected void trace(Throwable e) {
        while (e != null) {
            System.err.println(e.getClass().getName());
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.err.println("");
            e = e.getCause();
            if (e != null) {
                System.err.println("CAUSED BY: ");
                trace(e);
            }
        }
    }
}