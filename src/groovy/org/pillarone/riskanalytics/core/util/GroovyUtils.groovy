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

    static List toList(List list) {
        if (!isListOfList(list))
            return concatList(list)
        List<List> resultList = []
        list.each {List actList ->
            def item = actList.size() > 0 ? actList.get(0) : null
            def itemValue = item ? getItemValue(item) : null
            if (itemValue && (itemValue instanceof List)) {
                List tempList = concatList(actList)
                resultList << tempList
            } else {
                resultList << actList
            }
        }
        return resultList
    }

    protected static List concatList(List actList) {
        List tempList = []
        actList.each {it ->
            tempList = tempList + getItemValue(it)
        }
        return tempList
    }

    static def getItemValue(def item) {
        try {
            if (item instanceof String && item.indexOf("[") != -1 && item.indexOf("]") != -1) {
                return getGroovyShell().evaluate(item)
            }

        } catch (Exception ex) {
            return item
        }
    }


    private static List addQuoteToListItems(List list) {
        List resultList = []
        list.each {
            if (it instanceof String) {
                StringBuffer buffer = new StringBuffer("\"");
                buffer.append(it);
                buffer.append("\"");
                resultList << buffer.toString();
            } else {
                resultList << it.toString();
            }
        }
        return resultList
    }

    static List<List> getSplitList(List list, int max_tokens) {
        List resultList = []
        if (list.size() > 0 && list.get(0) instanceof List) {
            list.each {List actList ->
                if (actList.toString().length() > max_tokens) {
                    resultList << splitList(actList, max_tokens)
                } else {
                    resultList << addQuoteToListItems(actList)
                }
            }
        } else {
            if (list.toString().length() > max_tokens) {
                resultList = splitList(list, max_tokens)
            } else {
                resultList << addQuoteToListItems(list)
            }
        }
        return resultList
    }



    static List splitList(List list, int max_tokens) {
        List<List> resultList = []
        int semi = list.size() / 2
        def firstRange = 0..semi
        def secondRange = (semi + 1)..list.size() - 1
        List firstList = list.getAt(firstRange)
        List secondList = list.getAt(secondRange)
        resultList << "'" + addQuoteToListItems(firstList).toString() + "'"
        resultList << "'" + addQuoteToListItems(secondList).toString() + "'"
        return resultList
    }

    static boolean isListOfList(List list) {
        return (list && (list.get(0) instanceof List))
    }

    static String listToString(List list) {
        return list.toString();
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
