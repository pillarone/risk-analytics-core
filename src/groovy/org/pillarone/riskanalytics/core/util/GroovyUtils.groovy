package org.pillarone.riskanalytics.core.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.text.MessageFormat
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 *  This class contains methods which are easy/short to implement in Groovy, but unreadable in Java.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class GroovyUtils {

    private static Log LOG = LogFactory.getLog(GroovyUtils)

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
        if (list.size() == 1 && list[0].size() == 0) return []
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
            } else if (it instanceof DateTime) {
                resultList << "new ${DateTime.class.name}(${it.year},${it.monthOfYear},${it.dayOfMonth},0,0,0,0)"
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
        if (list.size() < max_tokens) {
            resultList << "'" + addQuoteToListItems(list).toString() + "'"
            return resultList
        }
        int index = max_tokens
        int startIndex = 0

        while (index < list.size() - 1) {
            List tempList = list.getAt(startIndex..index)
            resultList << "'" + addQuoteToListItems(tempList).toString() + "'"
            startIndex = index + 1
            index = (index + max_tokens < list.size()) ? index + max_tokens : list.size() - 1
        }
        if (index >= startIndex && index <= list.size() - 1)
            resultList << "'" + addQuoteToListItems(list.getAt(startIndex..index)).toString() + "'"

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

    static List<String> getEnumValuesFromClass(Class clazz) {
        List result = []
        if (clazz.isEnum()) {
            result = clazz.values()*.toString()
        }
        return result
    }

    static Long getId(def domainClass) {
        return domainClass.id
    }

    static Object numberValue(Class clazz, Object value) {
        switch (clazz) {
            case Integer.class: return value.intValue()
            case BigDecimal.class: return value.intValue()
            case BigInteger.class: return value.intValue()
            case Double.class: return value.doubleValue()
            case Float.class: return value.floatValue()
            case Long.class: return value.longValue()
            case Short.class: return value.shortValue()
            default: return value;
        }
    }

    public static Map<String, Object> getProperties(def object) {
        Map<String, Object> result = [:]

        Class currentClass = object.getClass()
        while (currentClass != Object.class) {
            Field[] fields = currentClass.declaredFields
            for (Field field in fields) {
                if (!Modifier.isStatic(field.modifiers)) {
                    field.accessible = true
                    result.put(field.name, field.get(object))
                }

            }
            currentClass = currentClass.superclass
        }

        if (object instanceof DynamicComposedComponent) {
            List<Component> componentList = object.componentList
            for (Component component in componentList) {
                result.put(component.name, component)
            }
        }

        return result
    }

    static Set getBundles(Locale locale) {
        def resourceBundle = []
        def resources = ResourceBundleRegistry.getValidationBundles()
        for (String bundleName in resources) {
            resourceBundle << ResourceBundle.getBundle(bundleName, locale)
        }
        return resourceBundle
    }

    public static String getText(String key, Object[] args, Locale locale) {
        String text = null
        Set<ResourceBundle> validationResourceBundles = getBundles(locale)
        if (validationResourceBundles) {
            for (ResourceBundle bundle: validationResourceBundles) {
                if (text == null) {
                    try {
                        text = bundle.getString(key)
                        text = MessageFormat.format(text, args)
                    } catch (Exception ex) { text = null}
                }
            }
        }
        return text != null ? text : key
    }

    public static parseGroovyScript(String scriptContent, Closure c) {
        ConfigSlurper configSlurper = new ConfigSlurper()
        Script script = configSlurper.classLoader.parseClass(scriptContent).newInstance()

        try {
            ConfigObject configObject = configSlurper.parse(script)
            c.call(configObject)
        } finally {
            try {
                GroovySystem.metaClassRegistry.removeMetaClass(script.class)
            } catch (Exception e) {
                LOG.error "Failed to remove meta class of script after using config slurper - possible memory leak."
            }
        }
    }
}
