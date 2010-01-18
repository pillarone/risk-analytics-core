package org.pillarone.riskanalytics.core.util

import umontreal.iro.lecuyer.rng.RandomStreamBase

public class DummyRandomStreamBase extends RandomStreamBase {
    double nextValue = 2
    boolean valid = true

    public void resetStartStream() {
        throw new Exception("resetStartStream not allowed")
    }

    public void resetStartSubstream() {
        throw new Exception("resetStartSubstream not allowed")
    }

    public void resetNextSubstream() {
        throw new Exception("resetNextSubstream not allowed")
    }

    public String toString() {
        return "next value will be ${nextValue}"
    }

    public void invalidate() {
        valid = false
    }

    protected double nextValue() {
        if (!valid) {
            throw new Exception("this generator is not valid")
        }
        double value = nextValue
        nextValue++
        return 1 / value
    }

    public RandomStreamBase clone() {
        throw new CloneNotSupportedException()
    }

}