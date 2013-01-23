package org.pillarone.riskanalytics.core.simulation.item.parameter;

public enum TestAbstractEnum {
    A {
        @Override
        public void doSomething() {
        }
    }, B {
        @Override
        public void doSomething() {
        }
    };

    public abstract void doSomething();
}