class DBCleanUpJob {

    def cronExpression = "0 0 0 * * ?" // call DBCleanUp every midnight

    /* frahman seeing these a lot on our sqlserver-based setup:

    [21.Sep.2013 00:00:01,789] - quartzScheduler_Worker-8 () - ERROR ExceptionPrinterJobListener Exception occured in job: GRAILS_JOBS.DBCleanUpJob
    org.quartz.JobExecutionException: Cannot invoke method cleanUp() on null object [See nested exception: java.lang.NullPointerException: Cannot invoke method cleanUp() on null object]
            at org.quartz.core.JobRunShell.run(JobRunShell.java:199)
            at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:546)
    Caused by: java.lang.NullPointerException: Cannot invoke method cleanUp() on null object
            at DBCleanUpJob.execute(DBCleanUpJob.groovy:8)

    The advice at http://stackoverflow.com/questions/8480509/grails-calling-service-error already seems to be known
    * */
    def dBCleanUpService

    def execute() {
        dBCleanUpService.cleanUp()
    }

}