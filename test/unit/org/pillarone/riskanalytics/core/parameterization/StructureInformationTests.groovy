package org.pillarone.riskanalytics.core.parameterization

class StructureInformationTests extends GroovyTestCase {

    void testNothing() {
        
    }

    //TODO:fix with new model
    /*void testGetLineForPacket() {
        StructureTestModel model = new StructureTestModel()
        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/test/StructureTestStructure", model)
        injector.injectConfiguration model

        StructureInformation information = new StructureInformation(injector.configObject, model)
        assertEquals "LOB1", information.getLine(new Claim(origin: model.mtpl.subClaimsGenerator))
        assertEquals "LOB1", information.getLine(new Claim(origin: model.mtpl))
        assertEquals "GLOBAL", information.getLine(new Claim(origin: model.claimsAggregator))
    }

    void testGetLineForComponent() {
        StructureTestModel model = new StructureTestModel()
        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/test/StructureTestStructure", model)
        injector.injectConfiguration model

        StructureInformation information = new StructureInformation(injector.configObject, model)
        assertEquals "LOB1", information.getLine(model.mtpl.subClaimsGenerator)
        assertEquals "LOB1", information.getLine(model.mtpl)
        assertEquals "GLOBAL", information.getLine(model.claimsAggregator)
    }

    void testGetComponentPath() {
        StructureTestModel model = new StructureTestModel()
        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/test/StructureTestStructure", model)
        injector.injectConfiguration model

        StructureInformation information = new StructureInformation(injector.configObject, model)
        assertEquals("StructureTest:LOB1:mtpl:subClaimsGenerator:outClaims", information.getPath(new Claim(origin: model.mtpl.subClaimsGenerator, sender: model.mtpl.subClaimsGenerator, senderChannelName: "outClaims")))
        assertEquals("StructureTest:GLOBAL:claimsAggregator:outClaims", information.getPath(new Claim(origin: model.claimsAggregator, sender: model.claimsAggregator, senderChannelName: "outClaims")))
        assertEquals("StructureTest:GLOBAL:claimsAggregator:outClaims", information.getPath(new Premium(origin: model.claimsAggregator, sender: model.claimsAggregator, senderChannelName: "outClaims")))
        assertEquals("StructureTest:GLOBAL:claimsAggregator:outClaims", information.getPath(new Commission(origin: model.claimsAggregator, sender: model.claimsAggregator, senderChannelName: "outClaims")))
    }

    void testGetComponentPathOnLobLevel() {
        CapitalEagleModel model = new CapitalEagleModel()
        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/capitalEagle/CapitalEagleStructure", model)
        injector.injectConfiguration model

        StructureInformation information = new StructureInformation(injector.configObject, model)
        assertEquals("CapitalEagle:mtpl:outClaims", information.getPath(new Claim(origin: model.mtpl, sender: model.mtpl, senderChannelName: "outClaims")))
        assertEquals("CapitalEagle:motorHull:outClaims", information.getPath(new Claim(origin: model.motorHull, sender: model.motorHull, senderChannelName: "outClaims")))
    }

    void testCompoundComponentsInLine() {

        CapitalEagleModel model = new CapitalEagleModel()
        model.init()
        model.injectComponentNames()

        ExampleLob lob1 = model.mtpl
        ExampleLob lob2 = model.motorHull

        ConfigObject structure = new ConfigObject()
        structure.company.mtpl.components.lob1 = lob1
        structure.company.motorHull.components.lob2 = lob2

        StructureInformation information = new StructureInformation(structure, model)

        Claim c = new Claim(origin: lob1)
        assertEquals "mtpl", information.getLine(c)

        c = new Claim(origin: lob1.subClaimsGenerator)
        assertEquals "mtpl", information.getLine(c)

        c = new Claim(origin: lob1.subRiProgram)
        assertEquals "mtpl", information.getLine(c)

        c = new Claim(origin: lob1.subRiProgram.subClaimsAggregator)
        assertEquals "mtpl", information.getLine(c)

        c = new Claim(origin: lob1.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator)
        assertEquals "mtpl", information.getLine(c)

        c = new Claim(origin: lob2)
        assertEquals "motorHull", information.getLine(c)

        c = new Claim(origin: lob2.subClaimsGenerator)
        assertEquals "motorHull", information.getLine(c)

        c = new Claim(origin: lob2.subRiProgram)
        assertEquals "motorHull", information.getLine(c)

        c = new Claim(origin: lob2.subRiProgram.subClaimsAggregator)
        assertEquals "motorHull", information.getLine(c)

        c = new Claim(origin: lob2.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator)
        assertEquals "motorHull", information.getLine(c)


    }

    void testStructureInformationExpandedWithModelProperties() {
        ExampleCompanyModel model = new ExampleCompanyModel()

        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector = new StructureInformationInjector("src/java/models/exampleCompany/ExampleCompanyStructure", model)
        injector.injectConfiguration model

        StructureInformation structureInformation = new StructureInformation(injector.configObject, model)

        ConfigObject extendedConfigObject = structureInformation.extendWithModelProperties(injector.configObject, model)
        assertEquals 3, extendedConfigObject.keySet().size()
        assertEquals 5, extendedConfigObject.company.keySet().size()
    }

    public void testGetLineForLobNotSpecifiedInStructureFile_POE_571() {
        Model model = new StructureTestModel()

        model.init()
        model.injectComponentNames()
        StructureInformationInjector injector1 = new StructureInformationInjector("src/java/models/test/StructureTestStructure", model)
        injector1.injectConfiguration model

        injector1.configObject.company.remove("LOB1")
        StructureInformation structureInformation = new StructureInformation(injector1.configObject, model)

        structureInformation.extendWithModelProperties(injector1.configObject, model)


        Claim c = new Claim(origin: model.mtpl)
        assertEquals "mtpl", structureInformation.getLine(c)
    }*/
}