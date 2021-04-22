package wixSpreadSheet

data class Data(
    @Transient var cellNotation: String,
    var value: Value? = null,
    var formula: Formula? = null,
    var error: String? = null
) {
}