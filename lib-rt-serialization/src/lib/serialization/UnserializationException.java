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

package lib.serialization;

public class UnserializationException extends Exception {
    public UnserializationException() {
    }

    public UnserializationException(String message) {
        super(message);
    }

    public UnserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnserializationException(Throwable cause) {
        super(cause);
    }
}
