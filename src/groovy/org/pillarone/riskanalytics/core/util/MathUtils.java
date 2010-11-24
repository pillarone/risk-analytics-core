package org.pillarone.riskanalytics.core.util;

import umontreal.iro.lecuyer.rng.F2NL607;
import umontreal.iro.lecuyer.rng.RandomStreamBase;

import java.io.IOException;
import java.io.ObjectOutputStream;
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

    public static double calculatePercentile(double[] values, double severity) {
        Arrays.sort(values);
        return calculatePercentileOfSortedValues(values, severity);
    }

    public static void setRandomStreamBase(F2NL607 base) {
        RANDOM_NUMBER_GENERATOR_INSTANCE.set(base);
    }

    public static F2NL607 getRandomStreamBase() {
        return RANDOM_NUMBER_GENERATOR_INSTANCE.get();
    }

    public static void initRandomStreamBase(Integer seed) {
        if (seed == null) {
            return;
        }
        F2NL607 generator = new F2NL607();
        generateSeed(generator, seed);
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
     * If an array is not yet sorted use Arrays.sort(values) before applying this function;
     *
     * @param sortedValues
     * @param severity
     * @return
     */
    public static double calculatePercentileOfSortedValues(double[] sortedValues, double severity) {
        severity = severity / 100d;
        int size = sortedValues.length;
        int g = (int) Math.floor((size + 1 / 3d) * severity + 1 / 3d);
        double gamma = (size + 1 / 3d) * severity + 1 / 3d - g;
        if (g == 0) {
            return sortedValues[0];
        }
        else if (g >= size) {
            return sortedValues[size - 1];
        }
        else {
            return (1 - gamma) * sortedValues[g - 1] + gamma * sortedValues[g];
        }
    }

    public static double calculateVar(double[] values, double severity) {
        return calculateVar(values, severity, calculateMean(values));
    }

    public static double calculateVarOfSortedValues(double[] sortedValues, double severity, double mean) {
        return calculatePercentileOfSortedValues(sortedValues, severity) - mean;
    }

    public static double calculateVar(double[] values, double severity, double mean) {
        Arrays.sort(values);
        return calculateVarOfSortedValues(values, severity, mean);
    }

    public static double calculateTvar(double[] values, double tvar) {
        Arrays.sort(values);
        return calculateTvarOfSortedValues(values, tvar);
    }

    /**
     * If an array is not yet sorted use Arrays.sort(values) before applying this function;
     *
     * @param sortedValues
     * @param severity
     * @return
     */
    public static double calculateTvarOfSortedValues(double[] sortedValues, double severity) {
        severity = severity / 100d;
        int size = sortedValues.length;
        int g = (int) Math.floor((size + 1 / 3d) * severity + 1 / 3d);
        double gamma = (size + 1 / 3d) * severity + 1 / 3d - g;
        int index = 0;
        if (gamma == 0) {
            index = Math.min(g - 1, size - 1);
        }
        else {
            index = Math.min(g, size - 1);
        }
        double sum = 0;
        for (int i = index; i < size; i++) {
            sum += sortedValues[i];
        }
        double mean = sum / (double) (size - index);

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