package org.pillarone.riskanalytics.core.packets

class ITestPacketOrange extends MultiValuePacket {

    public double a;
    public double b;

    @Override
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> map = new HashMap<String, Number>();
         map.put("a", a);
         map.put("b", b);
         return map;
     }
}
