import com.google.gson.Gson
import wixSpreadSheet.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


fun main() {

    val gson = Gson()
    val spreadSheet = gson.fromJson(makeGetRequest("https://www.wix.com/_serverless/hiring-task-spreadsheet-evaluator/jobs").body(), SpreadSheet::class.java)
    spreadSheet.execute()

    val evaluatedSpreadSheet = Submit(results = spreadSheet.jobs, email = "kajus.padelskas@gmail.com")
    val json = gson.toJson(evaluatedSpreadSheet)
    //val response = makePostRequest(url = spreadSheet.submissionUrl,postString = json).body()
    //println(response)
}

fun makeGetRequest(url: String): HttpResponse<String?>{
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url)).GET()
        .build()
    return client.send(request, HttpResponse.BodyHandlers.ofString())
}

fun makePostRequest(url: String, postString: String): HttpResponse<String?>{
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .POST(HttpRequest.BodyPublishers.ofString(postString)).header("Content-Type", "application/json; charset=utf-8").build()
    return client.send(request, HttpResponse.BodyHandlers.ofString())
}