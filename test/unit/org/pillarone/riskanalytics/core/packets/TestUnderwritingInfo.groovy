package org.pillarone.riskanalytics.core.packets

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TestUnderwritingInfo  extends MultiValuePacket{
    
    public TestUnderwritingInfo originalUnderwritingInfo;
    public double premiumWritten;
    public double commission;

    @Override
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> map = new HashMap<String, Number>();
         map.put("premium", premiumWritten);
         map.put("commission", commission);
         return map;
     }
    

}
