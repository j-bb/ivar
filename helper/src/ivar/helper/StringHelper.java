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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import static org.apache.commons.lang3.StringUtils.leftPad;

public final class StringHelper {

    public static final int STRINGBUILDER_THRESOLD = 10;
    public static final String DASH = "-";
    private static final Pattern doublePattern;

    static {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex
                = ("[\\x00-\\x20]*"
                + "[+-]?("
                + "NaN|"
                + "Infinity|"
                + "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
                + "(\\.(" + Digits + ")(" + Exp + ")?)|"
                + "(("
                + "(0[xX]" + HexDigits + "(\\.)?)|"
                + "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
                + ")[pP][+-]?" + Digits + "))"
                + "[fFdD]?))"
                + "[\\x00-\\x20]*");
        doublePattern = Pattern.compile(fpRegex);
    }

    private StringHelper() {
    }

    public static boolean isAllLowerCase(final String s) {
        boolean result = false;
        if (s != null) {
            result = s.matches("[^A-Z]*");
        }
        return result;
    }

    public static boolean isAllUpperCase(final String s) {
        boolean result = false;
        if (s != null) {
            result = s.matches("[^a-z]*");
        }
        return result;
    }

    public static int count(final String s, final char c) {
        int result = 0;
        if (s != null) {
            for (int i = s.length() - 1; i >= 0; i--) {
                if (s.charAt(i) == c) {
                    result++;
                }
            }
        }
        return result;
    }

    public static String insideFirstQuote(final String s) {
        String result = null;
        int openQuote = -1;
        int closeQuote = -1;
        if (s != null && s.contains("\"")) {
            for (int i = 0, length = s.length(); i < length; i++) {
                if (s.charAt(i) == '"') {
                    if (i != 0 && s.charAt(i - 1) != '\\') {
                        if (openQuote < 0) {
                            openQuote = i + 1;
                        } else {
                            closeQuote = i;
                            break;
                        }
                    } else if (i == 0) {
                        openQuote = 1;
                    }
                }
            }
            if (openQuote >= 0 && closeQuote > 0) {
                result = s.substring(openQuote, closeQuote);
                result = result.replace("\\\"", "\"");
            }
        }
        return result;
    }

    public static String remove(String s, char c) {
        String result = null;
        if (s != null) {
            result = s.replace(Character.toString(c), "");
        }
        return result;
    }

    public static String removeSpace(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        return remove(s, ' ');
    }

    public static String clean(String s) {
        return remove(removeSpace(s), ';');
    }

    public static String getFirstLowerCase(final String s) {
        String result = null;
        if (s != null) {
            final int length = s.length();
            if (length > 0) {
                String firstLetter = s.substring(0, 1).toLowerCase();
                if (length > 1) {
                    result = firstLetter + s.substring(1, length);
                } else {
                    result = firstLetter;
                }
            } else {
                result = s;
            }
        }
        return result;
    }

    public static String getFirstCapitalized(final String s) {
        String result = null;
        if (s != null) {
            final int length = s.length();
            if (length > 0) {
                String firstLetter = s.substring(0, 1).toUpperCase();
                if (length > 1) {
                    result = firstLetter + s.substring(1, length);
                } else {
                    result = firstLetter;
                }
            } else {
                result = s;
            }
        }
        return result;
    }

    public static String getFirstCapitalizedAndLoweredTheRest(final String s) {
        String result = null;
        if (s != null) {
            final int length = s.length();
            if (length > 0) {
                String firstLetter = s.substring(0, 1).toUpperCase();
                if (length > 1) {
                    result = firstLetter + s.substring(1, length).toLowerCase();
                } else {
                    result = firstLetter;
                }
            } else {
                result = s;
            }
        }
        return result;
    }

    public static String getLeft(final String string, final char separator) {
        return getLeft(string, Character.toString(separator));
    }

    public static String getLeft(final String string, final String separator) {
        if (string == null) {
            return null;
        }
        int ind = string.indexOf(separator);
        String left;
        if (ind > 0) {
            left = string.substring(0, ind);
        } else {
            left = null;
        }
        return left;
    }

    public static String getRight(final String string, final char separator) {
        return getRight(string, Character.toString(separator));
    }

    public static String getRight(final String string, final String separator) {
        if (string == null) {
            return null;
        }
        int ind = string.indexOf(separator);
        String right;
        if (ind >= 0) {
            right = string.substring(ind + separator.length(), string.length());
        } else {
            right = null;
        }
        return right;
    }

    public static String getLastRight(final String string, final char separator) {
        return getLastRight(string, Character.toString(separator));
    }

    public static String getLastRight(final String string, final String separator) {
        if (string == null) {
            return null;
        }
        int ind = string.lastIndexOf(separator);
        String right = null;
        if (ind > 0) {
            right = string.substring(ind + separator.length(), string.length());
        }
        return right;
    }

    public static String getLastLeft(final String string, final String separator) {
        if (string == null) {
            return null;
        }
        int ind = string.lastIndexOf(separator);
        String left = null;
        if (ind > 0) {
            left = string.substring(0, ind);
        }
        return left;
    }

    public static String getJavaString(final Object... o) {
        String result;
        if (o != null) {
            for (int i = 0; i < o.length; i++) {
                o[i] = getFirstCapitalizedAndLoweredTheRest(o[i].toString().replace(' ', '_'));
            }
            result = concat(o);
        } else {
            result = null;
        }
        return result;
    }

    public static String getXMLString(final Object... objects) {
        String result;
        if (objects != null) {
            final int slength = (2 * objects.length) - 1;
            final int lastDash = slength - 1;
            final Object[] strings = new String[slength];
            int j = 0;
            for (final Object o : objects) {
                strings[j] = o.toString().toLowerCase();
                j++;
                if (j < lastDash) {
                    strings[j] = DASH;
                    j++;
                }
            }
            result = concat(strings);
        } else {
            result = null;
        }
        return result;
    }

    public static String concat(final Object... objects) {
        String result = null;
        if (objects != null && objects.length < STRINGBUILDER_THRESOLD) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] != null) {
                    if (result != null) {
                        result += objects[i];
                    } else {
                        result = objects[i].toString();
                    }
                }
            }
        } else if (objects != null) {
            final StringBuilder b = new StringBuilder();
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] != null) {
                    b.append(objects[i]);
                }
            }
            result = b.toString();
        } else {
            result = null;
        }
        return result;
    }

    public static int getFirstNonNull(Object[] array) {
        int result = -1;
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                Object o = array[i];
                if (array[i] != null) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    //
    // SHA1 section from http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
    //
    private static String convertToHex(byte[] data) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(final String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = null;
//        try {
        md = MessageDigest.getInstance("SHA-1");
//        } catch (NoSuchAlgorithmException e) {
//            final String message = "[StringHelper] Can't find SHA-1 algo in the JVM";
//            throw new RuntimeException(message, e);
//        }
        byte[] sha1hash;// = new byte[40];
//        try {
        md.update(text.getBytes("UTF-8"), 0, text.length());
//        } catch (UnsupportedEncodingException e) {
//            final String message = "[StringHelper] Unsupporded encoding using iso-8859-1, I know ... it's crazy ...";
//            throw new RuntimeException(message, e);
//        }
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    //
    // END of SHA1 section
    //
    public static String ensureOneLastSemicolon(String s) {
        s = s.trim();
        String result = s;

        if (!s.endsWith(";")) {
            result += ";";
        } else if (s.endsWith(";;")) {
            result = StringHelper.ensureOneLastSemicolon(s.substring(0, s.length() - 2));
        }
        return result;
    }

    public static boolean doesMatchKeynameFormat(String s) {
        return s != null && s.matches("[a-z][a-zA-Z0-9_]*");
    }

    public static String getKeynameFormat(String s) {
        s = normalize(s);
        String result = null;

        if (s != null) {
            result = s.replaceAll("[^a-zA-Z0-9_ \t]", "");
            result = result.replaceAll("[ \t]+", "_");
            result = result.replaceAll("^[^a-zA-Z]", "");
            result = getFirstLowerCase(result);
        }
        return result;
    }

    public static String getCamel(final String s, final String separator) {
        final StringTokenizer st = new StringTokenizer(s, separator, false);
        final StringBuilder sb = new StringBuilder(s.length());
        while (st.hasMoreTokens()) {
            sb.append(StringHelper.getFirstCapitalizedAndLoweredTheRest(st.nextToken()));
        }
        return sb.toString();
    }

    public static String unCamel(final String s, final String separator) {
        return s.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    public static boolean doesMatchMinMaxFormat(String s) {
        return s != null && s.matches("([0-9]+)|\\*");
    }

    public static String getMinMaxFormat(String s) {
        String result = null;
        if (s != null) {
            if (s.trim().equals("*") || s.trim().equals("n")) {
                result = "*";
            } else {
                result = s.replaceAll("[^0-9]", "");
            }
        }
        return result;
    }

    public static boolean doesMatchScenarioDataFormat(String s) {
        return s != null && s.matches("[A-Z][a-zA-Z0-9_]*");
    }

    public static String normalize(String s) {
        if (s != null) {
            s = s.trim();
            s = Normalizer.normalize(s, Normalizer.Form.NFD);
            s = s.replaceAll("[^\\p{ASCII}]", "");
        }
        return s;
    }

    public static String getScenarioDataFormat(String s) {
        s = normalize(s);
        String result = null;

        if (s != null) {
            result = s.replaceAll("[^a-zA-Z0-9_ \t]", "");
            result = result.replaceAll("[ \t]+", "_");
            result = result.replaceAll("^[^a-zA-Z]", "");
            result = getFirstCapitalized(result);
        }
        return result;
    }

    public static boolean doesMatchScenarioTechDataFormat(String s) {
        return s != null && s.matches("[a-zA-Z][a-zA-Z0-9_]*");
    }

    public static String getScenarioTechDataFormat(String s) {
        s = normalize(s);
        String result = null;

        if (s != null) {
            result = s.replaceAll("[^a-zA-Z0-9_ \t]", "");
            result = result.replaceAll("[ \t]+", "_");
        }
        return result;
    }

    public static boolean doesMatchScenarioTechTypeFormat(String s) {
        return s != null && s.matches("");
    }

    public static String toUNICODE(final String s) {
        final int capacity = s.length();
        StringBuilder sb = new StringBuilder(capacity);
        for (int i = 0; i < capacity; i++) {
            sb.append("\\");
            sb.append("u00");
            sb.append(Integer.toHexString((int) s.charAt(i)).toUpperCase());
        }
        return sb.toString();
    }

    public static String getRandomString(final int length) {
        final StringBuffer sb = new StringBuffer();
        for (int i = length; i > 0; i -= 12) {
            final int n = Math.min(12, Math.abs(i));
            sb.append(leftPad(Long.toString(Math.round(Math.random() * Math.pow(36, n)), 36), n, '0'));
        }
        return sb.toString();
    }

    public static String getStringForJavaPackage(final String s) {
        String result = getKeynameFormat(s);
        remove(result, '-');
        return result.toLowerCase();
    }

    public static String getWithEscapedDoubleQuotes(final String s) {
        return s.replaceAll("\"", "\\\\\"");
    }

    public static String indentString(int indentLevel, final String s) {
        String indent = "";
        for (; indentLevel > 0; indentLevel--) {
            indent += " ";
        }

        return s.replaceAll("(\n)( *)([^\n]+)", "$1" + indent + "$3");
    }

    public static String quoteQuotes(final String s) {
        return s.replaceAll("\"", "\"\"");
    }

    public static boolean isDouble(final String s) {
        return doublePattern.matcher(s).matches();
    }
}
