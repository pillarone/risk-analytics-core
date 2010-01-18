package org.pillarone.riskanalytics.core.util

/**
 *  This class contains methods which are easy/short to implement in Groovy, but unreadable in Java.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class GroovyUtils {

    static GroovyShell shell

    public static double[] convertList(List list) {
        list as double[]
    }

    public static int[] convertIntList(List list) {
        list as int[]
    }

    public static List listWithOneVoidString() {
        ['']
    }

    /**
     * @return a list containing for each array element in a separate list.
     */
    public static List convertToListOfList(Object[] array) {
        def listOfList = [[]]
        listOfList.clear()
        for (Object obj: array) {
            def wrapper = []
            wrapper << obj
            listOfList << wrapper
        }
        listOfList
    }

    static void compareConfigObjects(ConfigObject o1, ConfigObject o2) {
        /*GroovyTestCase.assertEquals o1.keySet().size(), o2.keySet().size()
        o1.keySet().each {
            GroovyTestCase.assertTrue o2.keySet().contains(it)
        }*/
        GroovyTestCase.assertEquals(o1.toString(), o2.toString())
    }

    static List toList(String strList) {
        return (List) getGroovyShell().evaluate(strList)
    }

    static GroovyShell getGroovyShell() {
        if (shell == null) {
            shell = new GroovyShell()
        }
        return shell
    }

    static double[] asDouble(List list) {
        if (list == null || list.size() == 0) return []
        def item = list[0]
        if (!(item instanceof Number))
            return item[0] as double[]
        return list as double[]
    }
}
