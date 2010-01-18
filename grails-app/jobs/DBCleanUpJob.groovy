class DBCleanUpJob {

    def cronExpression = "0 0 0 * * ?" // call DBCleanUp every midnight

    def dBCleanUpService

    def execute() {
        dBCleanUpService.cleanUp()
    }

}