/*
    Author: Jakob Orel
    Date: 9/30/2021
    CSC315 Week 2 Process Waiting Simulation

    This program is designed to simulate a non-preemptive process. The processes will run through an experiment to
    determine how long they must wait before they are executed. Some basic summary statistics will be calculated on the
    wait times after the experiment as well as keeping track of how long the waiting line grows and shrinks. In this
    experiment, time is measured as a floating point number rather than actually using system time.
 */
import kotlin.math.ln
import kotlin.math.max

/*
    The data class for a Process has six properties.
    --The executionTime is a randomly determined service time for the process that does not change and has an average
        value centered around 3.0.
    --The intervalTime is a randomly determined value that does not change and represents the time between when the
        process arrives and when the previous process arrived with an average value centered around 5.0.
    --The arrivalTime is a calculated variable that determines when the process arrives using the previous process'
        arrivalTime and the current process' intervalTime.
    --The beginTime is a calculated variable that determines when the process will begin executing using the previous
        process' endTime and the current process' arrival time.
    --The endTime is a calculated variable that determines when the process will stop executing using the beginTime and
        executionTime.
    --The waitTime is a calculated variable that determines how long a process is waiting to execute based on its
        arrivalTime and beginTime.
 */
data class Process (val executionTime: Double, val intervalTime: Double){
    var arrivalTime: Double = 0.0
    var beginTime: Double = 0.0
    var endTime: Double = 0.0
    var waitTime: Double = 0.0
}

/*
    The createProcess function is used to create a new Process and provide the randomly determined values for
    executionTime and intervalTime. The values of executionMean and intervalMean can be changed to change the weight
    of the average for the random value. The myRandom lambda function uses the specified mean to get a random floating
    point number using the Math.random() function.
 */
fun createProcess():Process{
    val myRandom = { specifiedRandom:Double -> -specifiedRandom * ln(Math.random())}
    // average time for executionTime and intervalTime
    val executionMean = 3.0
    val intervalMean = 5.0
    return Process(myRandom(executionMean), myRandom(intervalMean))
}

/*
    The calculateOtherTimes function is used to calculate all the other properties of each Process other than the
    executionTime and intervalTime using the List of Processes. It is given the name of a queue, but it is really
    just a List. The variables of each Process are able to be modified because they are set as 'var' in the data class.
    As specified in the description of the Process data class each variable is calculated by using the previous process
    in the experiment. The values of the first process must be set before iterating through the list.
 */
fun calculateOtherTimes(queue: List<Process>){
    // Set the first times for the first process
    queue.first().arrivalTime = queue.first().intervalTime
    queue.first().beginTime = queue.first().arrivalTime
    queue.first().endTime = queue.first().beginTime + queue.first().executionTime
    // waitTime for first process is always 0
    // Set the times for each other process
    for (i in 1 until queue.size){
        queue[i].arrivalTime = queue[i-1].arrivalTime + queue[i].intervalTime
        queue[i].beginTime = max(queue[i].arrivalTime, queue[i-1].endTime)
        queue[i].endTime = queue[i].beginTime + queue[i].executionTime
        queue[i].waitTime = queue[i].beginTime - queue[i].arrivalTime
    }
}

/*
    The calculateLineLength function iterates through the list of Processes to determine the length of the line at each
    point of the experiment. The 'line' or queue is determined by how many processes have arrived but are currently
    still waiting to be executed. Each integer in the list indicates the number of processes that are waiting. This is
    determined by iterating through the list and then going backwards from the current process to see how many are
    also still waiting (by incrementing the current value for the lineCount). Once it finds one that is not waiting, it
    will break out of the backwards loop, add the lineCount to the lineList, and go to the next process.
 */
fun calculateLineLength(queue: List<Process>): List<Int>{
    val lineList = mutableListOf<Int>()
    for (process in 0 until queue.size){
        var lineCount = 0
        for (check in process downTo 0){
            if(queue[check].beginTime > queue[process].arrivalTime){
                lineCount += 1
            }
            else break
        }
        lineList.add(lineCount)
    }
    return lineList.toList()
}

// Extension of List<Double> to calculate Median of list.
fun List<Double>.median(): Double{
    val sortedList = this.sortedBy{ it }
    val middle = this.size / 2
    // If length of list is even, average the middle 2 values.
    if(this.size % 2 == 0){
        return ((sortedList[middle] + sortedList[middle-1]) / 2)
    }
    // Otherwise return the middle value of the sortedList.
    else{
        return sortedList[middle]
    }
}

// Extension of List<Double> to calculate Standard Deviation of a list.
fun List<Double>.sd(): Double {
    var standardDeviation = 0.0
    val mean = this.average()

    for (num in this) {
        standardDeviation += Math.pow(num - mean, 2.0)
    }

    return Math.sqrt(standardDeviation / this.size)
}

fun main() {
    // Create a processQueue (which is just a List of Processes) using the function of createProcess().
    // The size of the queue can be changed to create a longer list of processes.
    val processQueue: List<Process> = List<Process>(100){createProcess()}
    calculateOtherTimes(processQueue)

    // Taking a look at the arrivalTimes for each process.
    val arrivalTimes = processQueue.map{ it.arrivalTime }
    println("ArrivalTimes: $arrivalTimes")

    // Taking a look at the beginTimes for each process.
    val beginTimes = processQueue.map{ it.beginTime }
    println("BeginTimes: $beginTimes")

    // Taking a look at the endTimes for each process.
    val endTimes = processQueue.map{ it.endTime }
    println("EndTimes: $endTimes")

    // Taking a look at the waitTimes for each process.
    val waitTimes = processQueue.map{ it.waitTime }
    println("WaitTimes: $waitTimes")

    // Taking a look at the waitLine.
    val waitLine = calculateLineLength(processQueue)
    println("Length of line during experiment: $waitLine")
    println("Max length of line: ${waitLine.maxOrNull()}")

    // Calculating summary statistics for the waitTimes based on the experiment.
    println("-------Summary statistics-------")
    println("Count of processes: ${processQueue.count()}")
    println("Minimum wait time: ${waitTimes.minOrNull()}")
    println("Maximum wait time: ${waitTimes.maxOrNull()}")
    println("Median wait time: ${waitTimes.median()}")
    println("Average wait time: ${waitTimes.average()}")
    println("Standard deviation of wait time: ${waitTimes.sd()}")

}