package org.pillarone.riskanalytics.core.packets

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ITestPacketApple extends SingleValuePacket {

    private static final String ULTIMATE = "ultimate";

     public void plus(ITestPacketApple testPacketApple) {
        value += testPacketApple.value;
    }

     @Override
    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        Map<String, Number> valuesToSave = new HashMap<String, Number>(1);
        valuesToSave.put(ULTIMATE, value);
        return valuesToSave;
    }

}
