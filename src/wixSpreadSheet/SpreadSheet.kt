package wixSpreadSheet

data class SpreadSheet(
    val submissionUrl: String,
    val jobs: List<Job>
) {

    fun execute(){
        loopJobs()
        printJobResult()
    }

    fun printJobResult() {
        for(job in jobs){
            println("${job.id}:")
            for(row in job.data){
                for(cell in row){
                    println(cell.value)
                }
            }
        }
    }

    private fun loopJobs() {
        for(job in jobs){
            job.evaluateJob()
        }
    }
}