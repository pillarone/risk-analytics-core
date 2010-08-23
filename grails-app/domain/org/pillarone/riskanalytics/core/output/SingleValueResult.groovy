package org.pillarone.riskanalytics.core.output

import org.hibernate.FetchMode
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter

class SingleValueResult {

    SimulationRun simulationRun
    int period
    int iteration
    PathMapping path
    CollectorMapping collector
    FieldMapping field
    int valueIndex
    Double value

    static constraints = {
        period min: 0
        iteration min: 0
        path()
        value()
        collector nullable:true
        field nullable:true
    }

    static mapping = {
        id generator: 'identity'
        path lazy: false, fetchMode: FetchMode.JOIN
    }

    String toString() {
        "${path.pathName}, ${field.fieldName}, $value"
    }

    def beforeInsert = {
        writeFile()
    }


    private void writeFile(){
        ResultWriter foa=new ResultWriter();
        foa.init (simulationRun.id);
        HashMap<String,byte[]> transfer=new HashMap<String,byte[]>();
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        DataOutputStream dos=new DataOutputStream(bos);
        dos.writeInt (iteration);
        dos.writeDouble (value);
        transfer.put(path.id+"_"+period+"_"+field.id,bos.toByteArray());
        foa.writeResult (transfer);//
    }

    public void writePerfFile(){
        ResultWriter foa=new ResultWriter();
        HashMap<String,byte[]> transfer=new HashMap<String,byte[]>();
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        DataOutputStream dos=new DataOutputStream(bos);
        for (int i=1;i<=50000;i++){
            dos.writeInt(i);
            dos.writeDouble(Math.random());
        }
        transfer.put(path.id+"_"+period+"_"+field.id,bos.toByteArray());
        foa.init (simulationRun.id);
        foa.writeResult (transfer);
    }
  //
}
