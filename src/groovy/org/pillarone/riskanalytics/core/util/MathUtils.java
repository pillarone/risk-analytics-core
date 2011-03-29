package org.pillarone.riskanalytics.core.util;

import org.pillarone.riskanalytics.core.output.QuantilePerspective;
import umontreal.iro.lecuyer.rng.F2NL607;
import umontreal.iro.lecuyer.rng.RandomStreamBase;

import java.util.ArrayList;
import java.util.Arrays;

public class MathUtils {

    private static ThreadLocal<F2NL607> RANDOM_NUMBER_GENERATOR_INSTANCE = new ThreadLocal<F2NL607>() {

        @Override
        protected F2NL607 initialValue() {
            return new F2NL607();
        }

    };
    public static int DEFAULT_RANDOM_SEED = 2;

    /**
     * @param values
     * @param severity
     * @return
     * @deprecated Use calculatePercentile(double[] values, double severity, String profitOrLoss) instead
     */
    @Deprecated
    public static double calculatePercentile(double[] values, double severity) {
        return calculatePercentile(values, severity, QuantilePerspective.LOSS);
    }

    public static double calculatePercentile(double[] values, double severity, QuantilePerspective perspective) {
        Arrays.sort(values);
        return calculatePercentileOfSortedValues(values, severity, perspective);
    }

    public static void setRandomStreamBase(F2NL607 base) {
        RANDOM_NUMBER_GENERATOR_INSTANCE.set(base);
    }

    public static F2NL607 getRandomStreamBase() {
        return RANDOM_NUMBER_GENERATOR_INSTANCE.get();
    }

    public static void initRandomStreamBase(Integer seed) {
        initRandomStreamBase(seed, 0);
    }

    public static void initRandomStreamBase(Integer seed, Integer substream) {
        if (seed == null) {
            return;
        }
        F2NL607 generator = new F2NL607();
        generateSeed(generator, seed);
        for (int i = 0; i < substream; i++) {
            generator.resetNextSubstream();
        }
        setRandomStreamBase(generator);
    }

    private static void generateSeed(F2NL607 generator, int seed) {
        double source = Math.PI * (Math.log10(seed) + 1);
        ArrayList<Integer> seedsList = new ArrayList<Integer>();
        for (int i = 0; i < 19; i++) {
            seedsList.add((int) source);
            source = (source - seedsList.get(seedsList.size() - 1)) * (100 + Math.E);
        }
        int[] seeds = new int[seedsList.size()];

        for (int i = 0; i < seedsList.size(); i++) {
            seeds[i] = seedsList.get(i);
        }
        generator.setLinearSeed(seeds);

        seedsList = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            seedsList.add((int) source);
            source = (source - seedsList.get(seedsList.size() - 1)) * (100 + Math.E);
        }

        seeds = new int[seedsList.size()];

        for (int i = 0; i < seedsList.size(); i++) {
            seeds[i] = seedsList.get(i);
        }

        generator.setNonLinearSeed(seeds);
    }

    /**
     * Clones the provided stream and selects defined subStream. If subStream is not positive, the stream
     * itselfs is returned.
     *
     * @param stream
     * @param subStream
     * @return
     */
    public static RandomStreamBase getRandomStream(RandomStreamBase stream, int subStream) {
        if (subStream > 0) {
            RandomStreamBase streamStartingAtSubstream = stream.clone();
            streamStartingAtSubstream.resetStartStream();
            for (int i = 0; i < subStream; i++) {
                streamStartingAtSubstream.resetNextSubstream();
            }
            return streamStartingAtSubstream;
        }
        else {
            return stream;
        }
    }

    /**
     * Simple Hashcode with image [0,10^3] and dilation by 100. Required for portfolio runs with several deals.
     * 1. The deals have to be independent. Therefore, each deal uses a different substream of the basic random stream
     * (cf. method initRandomStreamBase()). The respective substream is obtained by using the parametrization identity
     * associated uniquely to each deal and hashing this identity number to a smaller integer between [0,10^3].
     * Equating parametrization id and substream may be numerically too expensive.
     * 2. For parallel computing each deal requires a bunch of substreams. Here, for each deal we reserve 100 different substreams
     * obtained by stretching the hash value by a factor of 100.
     *
     * @param parametrizationId
     * @return
     */

    public static int determineSubstream(int parametrizationId) {
        int divisor = 1000;
        int quotient = (int) Math.floor(parametrizationId / (double) divisor);
        int remainder = parametrizationId - quotient * parametrizationId;
        return remainder * 100;
    }

    /**
     * If an array is not yet sorted use Arrays.sort(values) before applying this function;
     *
     * @param sortedValues
     * @param severity
     * @return
     */
    public static double calculatePercentileOfSortedValues(double[] sortedValues, double severity) {
        return calculatePercentileOfSortedValues(sortedValues, severity, QuantilePerspective.LOSS);
    }

    /**
     * Implementation of Hyndman-Fan quantile estimator as given by
     * Kim & Hardy: "Quantifying and correcting the bias in estimated risk measures"
     * @param sortedValues
     * @param severity
     * @param perspective distinction between random loss and random profit
     * @return
     */

    public static double calculatePercentileOfSortedValues(double[] sortedValues, double severity, QuantilePerspective perspective) {
        severity = severity / 100d;
        int size = sortedValues.length;
        int g = (int) Math.floor((size + 1 / 3d) * severity + 1 / 3d);
        double gamma = (size + 1 / 3d) * severity + 1 / 3d - g;
        switch (perspective) {
            case LOSS:
                if (g == 0) {
                    return sortedValues[0];
                }
                else if (g >= size) {
                    return sortedValues[size - 1];
                }
                else {
                    return (1 - gamma) * sortedValues[g - 1] + gamma * sortedValues[g];
                }
            case PROFIT:
                if (g == 0) {
                    return sortedValues[size - 1];
                }
                else if (g >= size) {
                    return sortedValues[0];
                }
                else {
                    return (1 - gamma) * sortedValues[size - g] + gamma * sortedValues[size - (g + 1)];
                }
            default:
                throw new IllegalArgumentException("percentile is calculated for loss or profit distribution: specify accordingly!");
        }
    }


    public static double calculateVar(double[] values, double severity) {
        return calculateVar(values, severity, calculateMean(values));
    }

    public static double calculateVar(double[] values, double severity, QuantilePerspective perspective) {
        return calculateVar(values, severity, calculateMean(values), perspective);
    }

    public static double calculateVarOfSortedValues(double[] sortedValues, double severity, double mean) {
        return calculatePercentileOfSortedValues(sortedValues, severity) - mean;
    }

    public static double calculateVarOfSortedValues(double[] sortedValues, double severity, double mean, QuantilePerspective perspective) {
        return calculatePercentileOfSortedValues(sortedValues, severity, perspective) - mean;
    }

    public static double calculateVar(double[] values, double severity, double mean) {
        return calculateVar(values, severity, mean, QuantilePerspective.LOSS);
    }

    public static double calculateVar(double[] values, double severity, double mean, QuantilePerspective perspective) {
        Arrays.sort(values);
        return calculateVarOfSortedValues(values, severity, mean, perspective);
    }

    public static double calculateTvar(double[] values, double severity) {
        return calculateTvar(values, severity, QuantilePerspective.LOSS);
    }

    public static double calculateTvar(double[] values, double severity, QuantilePerspective perspective) {
        Arrays.sort(values);
        return calculateTvarOfSortedValues(values, severity, perspective);
    }

    /**
     * If an array is not yet sorted use Arrays.sort(values) before applying this function;
     *
     * @param sortedValues
     * @param severity
     * @return
     */
    public static double calculateTvarOfSortedValues(double[] sortedValues, double severity) {
        return calculateTvarOfSortedValues(sortedValues, severity, QuantilePerspective.LOSS);
    }

    /**
     * Implementaion of conditional tail expectation usually independent of quantile estimator.
     * General suggestion with index=Math.floor(size*severity), cf.
     * Kim & Hardy: "Quantifying and correcting the bias in estimated risk measures"
     *
     * @param sortedValues
     * @param severity
     * @param perspective
     * @return
     */
    public static double calculateTvarOfSortedValues(double[] sortedValues, double severity, QuantilePerspective perspective) {
        severity = severity / 100d;
        int size = sortedValues.length;
        int index = (int) Math.floor(size*severity);
        double sum = 0;
        switch (perspective) {
            case LOSS:
                for (int i = index; i < size; i++) {
                    sum += sortedValues[i];
                }
                break;
            case PROFIT:
                for (int i = index; i < size; i++) {
                    sum += sortedValues[size - (i + 1)];
                }
                break;
            default:
                throw new IllegalArgumentException("TVaR is calculated for loss or profit distribution: specify accordingly!");
        }
        double mean = sum / (size*(1-severity));
        return mean - (calculateSum(sortedValues) / (double) size);
    }

    public static double calculateStandardDeviation(double[] values) {
        return calculateStandardDeviation(values, calculateMean(values));
    }

    public static double calculateStandardDeviation(double[] values, double mean) {
        double sqrtSum = 0.0;
        for (double value : values) {
            sqrtSum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sqrtSum / values.length);
    }

    public static double calculateMean(double[] values) {
        return calculateSum(values) / values.length;
    }

    public static double calculateSum(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    public static double max(double[] values) {
        Arrays.sort(values);
        return maxOfSortedValues(values);
    }

    public static double maxOfSortedValues(double[] sortedValues) {
        return sortedValues[sortedValues.length - 1];
    }

    public static double min(double[] values) {
        Arrays.sort(values);
        return minOfSortedValues(values);
    }

    public static double minOfSortedValues(double[] sortedValues) {
        return sortedValues[0];
    }

}