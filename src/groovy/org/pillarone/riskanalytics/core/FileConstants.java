package org.pillarone.riskanalytics.core;

import org.pillarone.riskanalytics.core.util.PropertiesUtils;

import java.io.File;

public class FileConstants {

    public static final String BASE_DATA_DIRECTORY;
    public static final String LOG_DIRECTORY;
    public static final String TEMP_FILE_DIRECTORY;
    public static final String EXTERNAL_DATABASE_DIRECTORY;
    public static final String GRIDGAIN_HOME;

    static {
        String baseDir = System.getProperty("p1.temp");
        StringBuilder builder = new StringBuilder(baseDir != null ? baseDir : System.getProperty("user.home"));
        builder.append(File.separatorChar);
        builder.append(".pillarone");
        builder.append(File.separatorChar);
        builder.append("RiskAnalytics-");
        String appVersion = new PropertiesUtils().getProperties("/version.properties").getProperty("version", "");
        builder.append(appVersion);
        BASE_DATA_DIRECTORY = builder.toString();
        File file = new File(BASE_DATA_DIRECTORY);
        file.mkdirs();
        assert file.exists();

        LOG_DIRECTORY = BASE_DATA_DIRECTORY + File.separatorChar + "logs";
        file = new File(LOG_DIRECTORY);
        file.mkdirs();
        assert file.exists();

        TEMP_FILE_DIRECTORY = BASE_DATA_DIRECTORY + File.separatorChar + "temp";
        file = new File(TEMP_FILE_DIRECTORY);
        file.mkdirs();
        assert file.exists();

        EXTERNAL_DATABASE_DIRECTORY = BASE_DATA_DIRECTORY + File.separatorChar + "database";
        file = new File(EXTERNAL_DATABASE_DIRECTORY);
        file.mkdirs();
        assert file.exists();

        GRIDGAIN_HOME = BASE_DATA_DIRECTORY + File.separatorChar + "gridgain";
        file = new File(GRIDGAIN_HOME);
        file.mkdirs();
        assert file.exists();
    }
}
