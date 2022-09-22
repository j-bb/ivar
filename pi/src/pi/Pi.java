/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pi;

import java.math.BigDecimal;

/**
 *
 * @author jbb
 */
public class Pi {

    private static final float MS_IN_SEC = 1000f;
    private static final float S_IN_MIN = 60f;
    private static final int INT_FIVE = 5;
    private static final int INT_239 = 239;
    private static final int FREQUENCY_DIAGRAM_SIZE = 10;

    public static void main(String args[]) throws NumberFormatException {
        //computePi(10000);
        computePrime(200000);
    }

    private static void computePrime(final int max) {
        final long start = System.currentTimeMillis();

        int nbPrime = 0;
        for (int n = 2; n < max; n++) {
            final int m_max = n - 1;
            boolean prime = true;
            for (int m = 2; m < m_max; m++) {
                if (n % m == 0) {
                    prime = false;
                    break;
                }
            }
            if (prime) {
                nbPrime++;
                System.out.println("n " + n);
            }
        }

        final long stop = System.currentTimeMillis();

        final long delta = stop - start;
        final float deltaSec = delta / MS_IN_SEC;
        final float deltaMin = deltaSec / S_IN_MIN;
        final String timeTrace = "[TIME] for " + max + " is " + delta + " ms, or " + deltaSec + " s, or " + deltaMin + " min.";
        System.out.println(timeTrace);
        System.out.println("For "+max+" found "+nbPrime+" primes, "+(double)((double)nbPrime / (double)max)*100+"%");
    }

    private static void computePi(final int digits) {
        final long start = System.currentTimeMillis();
        final BigDecimal bigDecimal = pi(digits);
        final long stop = System.currentTimeMillis();

        final long delta = stop - start;
        final float deltaSec = delta / MS_IN_SEC;
        final float deltaMin = deltaSec / S_IN_MIN;
        final String timeTrace = "[TIME] for " + digits + " is " + delta + " ms, or " + deltaSec + " s, or " + deltaMin + " min.";

        final String pi = bigDecimal.toString();

        System.out.println(pi);
        System.out.println(timeTrace);
        System.out.println("");
        System.out.println("Frequency diagram");
        System.out.println("");
        int freq[] = new int[FREQUENCY_DIAGRAM_SIZE];
        for (int i = 0; i < FREQUENCY_DIAGRAM_SIZE; i++) {
            freq[i] = 0;
        }
        int c;
        for (int i = 0; i < pi.length(); i++) {
            c = pi.charAt(i);
            if (c == '.') {
                continue;
            }
            c -= '0';
            freq[c]++;
        }
        for (int i = 0; i < FREQUENCY_DIAGRAM_SIZE; i++) {
            System.out.println("" + i + " " + freq[i]);
        }
    }

    /**
     * constants used in pi computation
     */
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    /**
     * rounding mode to use during pi computation
     */
    private static final int roundingMode = BigDecimal.ROUND_HALF_EVEN;

    /**
     * Compute the value of pi to the specified number of digits after the decimal point. The value is computed using Machin's formula:
     * <p/>
     * pi/4 = 4*arctan(1/5) - arctan(1/239)
     * <p/>
     * and a power series expansion of arctan(x) to sufficient precision.
     */
    public static BigDecimal pi(final int digits) {
        final int scale = digits + INT_FIVE;
        final BigDecimal arctan1_5 = arctan(INT_FIVE, scale);
        final BigDecimal arctan1_239 = arctan(INT_239, scale);
        final BigDecimal pi = arctan1_5.multiply(FOUR).subtract(arctan1_239).multiply(FOUR);
        final BigDecimal result = pi.setScale(digits, BigDecimal.ROUND_HALF_UP);
        return result;
    }

    /**
     * Compute the value, in radians, of the arctangent of the inverse of the supplied integer to the specified number of digits after the decimal point. The
     * value is computed using the power series expansion for the arc tangent:
     * <p/>
     * arctan(x) = x - (x^3)/3 + (x^5)/5 - (x^7)/7 + (x^9)/9 ...
     */
    public static BigDecimal arctan(final int inverseX, final int scale) {
        BigDecimal result, numer, term;
        final BigDecimal invX = BigDecimal.valueOf(inverseX);
        final BigDecimal invX2 = BigDecimal.valueOf(inverseX * inverseX);

        numer = BigDecimal.ONE.divide(invX, scale, roundingMode);

        result = numer;
        int i = 1;
        do {
            numer = numer.divide(invX2, scale, roundingMode);
            final int denom = 2 * i + 1;
            term = numer.divide(BigDecimal.valueOf(denom), scale, roundingMode);
            if ((i % 2) != 0) {
                result = result.subtract(term);
            } else {
                result = result.add(term);
            }
            i++;
        } while (term.compareTo(BigDecimal.ZERO) != 0);
        return result;
    }
}
