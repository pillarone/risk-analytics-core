package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleEnum
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

class ParameterWriterTests extends GroovyTestCase {

    File exportFile = new File("exportParam.groovy")

    protected void setUp() {
        super.setUp()
        exportFile.delete()
    }

    protected void tearDown() {
        exportFile.delete()
    }

    void testWriteConfigObject() {

        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreParameters")
        ConfigObject configObject = injector.configObject

        Writer writer = exportFile.newWriter()
        new ParameterWriter().write(configObject, writer)

        ConfigObject reloadedConfigObject = new ParameterInjector("exportParam").configObject

        assertNotNull(reloadedConfigObject)
        assertSameStructure(configObject, reloadedConfigObject)

    }

    //will be deleted anyway
    /* void testWriteModifiedConfigObject() {

        ParameterInjector injector = new ParameterInjector("src/java/models/test/StructureTestParameters")
        ConfigObject configObject = injector.configObject


        Model model = new StructureTestModel()
        model.initComponents()
        ParameterTreeBuilder builder = new ParameterTreeBuilder(model, "src/java/models/test/StructureTestStructure")
        builder.buildTree("src/java/models/test/StructureTestParameters")

        BufferedWriter writer = exportFile.newWriter()
        new ParameterWriter().write(builder.parameterConfigObject, writer)

        ConfigObject reloadedConfigObject = new ParameterInjector("exportParam").configObject

        assertNotNull(reloadedConfigObject)
        assertSameStructure(configObject, reloadedConfigObject)

    }*/

    void assertSameStructure(ConfigObject original, ConfigObject reloaded) {
        assertEquals("size of reloaded ${reloaded} configObject does not match original ${original}", original.size(), reloaded.size())
        original.each {key, value ->
            if (value instanceof ConfigObject) {
                assertSameStructure(value, reloaded[key])
            }
            assertTrue("key $key not found in map", reloaded.containsKey(key))
        }
    }

    void testWritePackage() {
        ConfigObject configObject = new ConfigObject()
        configObject.model = EmptyModel
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.write configObject, bufferedWriter
        assertTrue(writer.toString().startsWith(EmptyModel.getPackage().toString()))
    }

    void testWriteLineNoKeyTransformation() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("foo", "bar", bufferedWriter, "value")
        bufferedWriter.flush()
        assertTrue("keyString unchanged", writer.toString().startsWith("bar="))
    }

    void testWriteLineTransformKey() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, "value")
        bufferedWriter.flush()
        assertTrue("last element of key should be in square bracets", writer.toString().startsWith("a.b.c[key]="))
    }

    void testWriteLineWriteParameterObject() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, [getType: {-> ExampleParameterObjectClassifier.TYPE0}, getParameters: {-> [:]}] as IParameterObject)
        bufferedWriter.flush()
        assertEquals(ExampleParameterObjectClassifier.TYPE0.getConstructionString([:]), writer.toString().tokenize("=")[1].trim())

    }

    void testWriteLineWriteClassObject() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, this.class)
        bufferedWriter.flush()
        assertEquals(this.class.name, writer.toString().tokenize("=")[1].trim())

    }

    void testWriteLineWriteEnumeration() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, ExampleEnum.FIRST_VALUE)
        bufferedWriter.flush()
        assertEquals("${ExampleEnum.name}.${ExampleEnum.FIRST_VALUE.toString()}", writer.toString().tokenize("=")[1].trim())
    }

    void testWriteLineWriteBoolean() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, Boolean.TRUE)
        bufferedWriter.flush()
        assertEquals("true", writer.toString().tokenize("=")[1].trim())

        writer = new StringWriter()
        bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, Boolean.FALSE)
        bufferedWriter.flush()
        assertEquals("false", writer.toString().tokenize("=")[1].trim())
    }

    void testWriteLineWriteStandard() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, this)
        bufferedWriter.flush()
        assertEquals("toString on value should be called", this.toString(), writer.toString().tokenize("=")[1].trim())
    }

    void testWriteLineWriteList() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, [])
        bufferedWriter.flush()
        assertEquals("toString on value should be called", "[]", writer.toString().tokenize("=")[1].trim())

        writer = new StringWriter()
        bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, ["a", "b"])
        bufferedWriter.flush()
        assertEquals("toString on value should be called", "[\"a\",\"b\"]", writer.toString().tokenize("=")[1].trim())
    }

    void testWriteLineWriteString() {
        ParameterWriter parameterWriter = new ParameterWriter()
        StringWriter writer = new StringWriter()
        BufferedWriter bufferedWriter = new BufferedWriter(writer)
        def value = 1
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, "$value")
        bufferedWriter.flush()

        assertEquals("toString on value should be called", "\'1\'", writer.toString().tokenize("=")[1].trim())

        writer = new StringWriter()
        bufferedWriter = new BufferedWriter(writer)
        parameterWriter.writeLine("key", "a.b.c.key", bufferedWriter, '')
        bufferedWriter.flush()
        assertEquals("toString on value should be called", "''", writer.toString().tokenize("=")[1].trim())
    }


    public String toString() {
        "myToString"
    }


}