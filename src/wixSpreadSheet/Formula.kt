package wixSpreadSheet

data class Formula(
    val and: List<Reference>? = null,
    val concat: List<Reference>? = null,
    val divide: List<Reference>? = null,
    val `if`: List<Formula>? = null,
    val is_equal: List<Reference>? = null,
    val is_greater: List<Reference>? = null,
    val multiply: List<Reference>? = null,
    val not: Reference? = null,
    val or: List<Reference>? = null,
    val reference: String? = null,
    val sum: List<Reference>? = null,
    val isLess: List<Reference>? = null
)