package org.pillarone.riskanalytics.core.packets

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TestPacketApple extends SingleValuePacket {

    private double ultimate;
    private static final String ULTIMATE = "ultimate";

     public void plus(TestPacketApple testPacketApple) {
        ultimate += testPacketApple.getUltimate();
    }

     @Override
    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        Map<String, Number> valuesToSave = new HashMap<String, Number>(1);
        valuesToSave.put(ULTIMATE, ultimate);
        return valuesToSave;
    }

     public double getUltimate() {
        return ultimate;
    }

    public void setUltimate(double ultimate) {
        this.ultimate = ultimate;
    }

    @Deprecated
    public double getValue() {
        return ultimate;
    }

    
}