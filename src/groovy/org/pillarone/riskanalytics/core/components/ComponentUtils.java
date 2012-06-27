package org.pillarone.riskanalytics.core.components;

/**
 * Contains utility methods for conversion between model and display names. Usage examples can be found in the
 * corresponding test class ComponentUtilsTests.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class ComponentUtils {

    public static final String PATH_SEPARATOR = ":";
    public static final String SUB = "sub";
    public static final String PARM = "parm";
    public static final String OUT = "out";
    public static final String GLOBAL = "global";
    public static final String RUNTIME = "runtime";

    public static String getNormalizedPath(String path, String pathSeparator) {
        String[] pathElements = path.split(PATH_SEPARATOR);
        StringBuilder normalizedPath = new StringBuilder();
        for (String pathElement : pathElements) {
            if (pathElement != null) {
                if (!normalizedPath.toString().isEmpty()) {
                    normalizedPath.append(pathSeparator);
                }
                normalizedPath.append(getNormalizedName(pathElement));
            }
        }
        return normalizedPath.toString();
    }

    public static String getNormalizedName(String componentName) {
        String value = removeNamingConventions(componentName);
        StringBuilder displayNameBuffer = new StringBuilder();
        for(int characterIndex = 0; characterIndex < value.length(); characterIndex++) {
            displayNameBuffer.append(addSpaceIfRequired(value, characterIndex)).append(value.charAt(characterIndex));
        }
        return displayNameBuffer.toString();
    }

    private static final String VOID_STRING = "";
    private static final String SPACE = " ";

    private static String addSpaceIfRequired(String value, int index) {
        if (index == 0) return VOID_STRING;
        char precedingChar = value.charAt(index - 1);
        char thisChar = value.charAt(index);
        if (Character.isLowerCase(precedingChar) && Character.isUpperCase(thisChar)
                || Character.isLetter(precedingChar) && Character.isDigit(thisChar)
                || Character.isDigit(precedingChar) && Character.isLetter(thisChar)) {
            return SPACE;
        }
        if (index + 1 == value.length()) return VOID_STRING;
        char nextChar = value.charAt(index + 1);
        if (Character.isUpperCase(precedingChar) && Character.isUpperCase(thisChar) && Character.isLowerCase(nextChar)) {
            return SPACE;
        }
        return VOID_STRING;
    }

    public static String removeNamingConventions(String componentName) {
        if (componentName == null) {
            return null;
        } else if (componentName.startsWith(SUB)) {
            return componentName.substring(3);
        } else if (componentName.startsWith(PARM)) {
            return componentName.substring(4);
        } else if (componentName.startsWith(OUT)) {
            return componentName.substring(3);
        } else if (componentName.startsWith(GLOBAL)) {
            return componentName.substring(6);
        } else if (componentName.startsWith(RUNTIME)) {
            return componentName.substring(7);
        }
        return componentName;
    }

    public static String getComponentNormalizedName(String path) {
        String[] subComponents = path.split(PATH_SEPARATOR);
        return subComponents[subComponents.length - 1];
    }

}
