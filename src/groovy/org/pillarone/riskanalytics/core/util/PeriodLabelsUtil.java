package org.pillarone.riskanalytics.core.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.output.SimulationRun;
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter;
import org.pillarone.riskanalytics.core.simulation.LimitedContinuousPeriodCounter;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.core.simulation.item.Simulation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class PeriodLabelsUtil {

    public static final String PARAMETER_DISPLAY_FORMAT = "MMM dd, yyyy";

    static public List<String> getPeriodLabels(Simulation simulation, Model simulationModel) {
        return getPeriodLabels(simulation.getParameterization().getPeriodLabels(), simulation.getBeginOfFirstPeriod(),
                simulation.getPeriodCount(), simulationModel);
    }

    public static List<String> getPeriodLabels(List<String> periodLabels, SimulationRun simulationRun, Model simulationModel) {
        return getPeriodLabels(periodLabels, simulationRun.getBeginOfFirstPeriod(), simulationRun.getPeriodCount(), simulationModel);
    }

    static public List<String> getPeriodLabels(List<String> periodLabels, DateTime beginOfFirstPeriod,
                                               int numberOfPeriods, Model simulationModel) {
        List<String> modifiedPeriodLabels = new ArrayList<String>();
        if (periodLabels != null && !periodLabels.isEmpty()) {
            //Whenever possible, use the saved period labels
            DateTimeFormatter formatter = DateTimeFormat.forPattern(PARAMETER_DISPLAY_FORMAT);
            DateTimeFormatter parser = DateTimeFormat.forPattern(Parameterization.PERIOD_DATE_FORMAT);
            for(String periodLabel : periodLabels) {
                try {
                    String formattedPeriodLabel = formatter.print(parser.parseDateTime(periodLabel));
                    modifiedPeriodLabels.add(formattedPeriodLabel);
                }
                catch (IllegalArgumentException ex) {
                    // as parse failed we keep the provided period label
                    modifiedPeriodLabels.add(periodLabel);
                }
            }

            // if all periods start first of January, display only year as period label
            boolean allPeriodStartFirstJanuary = true;
            for (String periodLabel : modifiedPeriodLabels) {
                String[] dateSplitted = periodLabel.split(" ");
                if (dateSplitted.length == 3) {
                    allPeriodStartFirstJanuary &= dateSplitted[0].equals("Jan") && dateSplitted[1].equals("01,");
                }
                else {
                    allPeriodStartFirstJanuary = false;
                    break;
                }
            }
            if (allPeriodStartFirstJanuary) {
                Integer simulationStartYear = null;
                if (beginOfFirstPeriod == null) {
                    IPeriodCounter periodCounter = simulationModel.createPeriodCounter(beginOfFirstPeriod);
                    if (periodCounter instanceof LimitedContinuousPeriodCounter) {
                        simulationStartYear = Integer.valueOf(periodLabels.get(0).substring(0, 4));
                    }
                }
                else {
                    simulationStartYear = beginOfFirstPeriod.getYear();
                }
                if (simulationStartYear != null) {
                    modifiedPeriodLabels.clear();
                    for (String periodLabel : periodLabels) {
                        modifiedPeriodLabels.add(String.valueOf(simulationStartYear++));
                    }
                }
            }
        }
        else {
            //Saving period labels is not possible for certain period counters.. they have to be resolved here
            SimpleDateFormat format = new SimpleDateFormat(PARAMETER_DISPLAY_FORMAT);
            IPeriodCounter periodCounter = simulationModel.createPeriodCounter(beginOfFirstPeriod);
            if (periodCounter != null) {
                periodCounter.reset();
                for (int period = 0; period < numberOfPeriods; period++) {
                    modifiedPeriodLabels.add(format.format(periodCounter.getCurrentPeriodStart().toDate()));
                    periodCounter.next();
                }
            }
            else {
                for (int period = 0; period < numberOfPeriods; period++) {
                    modifiedPeriodLabels.add("P" + period);
                }
            }
        }
        return modifiedPeriodLabels;
    }
}
