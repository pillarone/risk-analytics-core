package org.pillarone.riskanalytics.core.util;

import umontreal.iro.lecuyer.rng.F2NL607;

public class DummyGenerator extends F2NL607 {

    @Override
    protected double nextValue() {
        return super.nextValue();
    }

    @Override
    public void nextArrayOfDouble(double[] u, int start, int n) {
        super.nextArrayOfDouble(u, start, n);
    }

    @Override
    public void nextArrayOfInt(int i, int j, int[] u, int start, int n) {
        super.nextArrayOfInt(i, j, u, start, n);
    }

    @Override
    public double nextDouble() {
        return super.nextDouble();
    }

    @Override
    public int nextInt(int i, int j) {
        return super.nextInt(i, j);
    }

    @Override
    public void resetNextSubstream() {
        super.resetNextSubstream();
    }

    @Override
    public void resetStartSubstream() {
        super.resetStartSubstream();
    }

    @Override
    public void resetStartStream() {
        super.resetStartStream();
    }
}
