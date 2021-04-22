package wixSpreadSheet

import java.lang.Exception
import kotlin.math.roundToInt

data class Job(
    val id: String,
    var `data`: List<List<Data>>
){
    @Transient var notationValues: HashMap<String,Value?> = HashMap()

    private fun setNotation() {
        var nextNotation: MutableList<Char>
        notationValues = HashMap() // initializing to not null
        for((cellPos, value) in data.withIndex()){
            nextNotation = ArrayList() // initializing to not null
            nextNotation.add('A')
            for(cell in value){

                if(cell.value != null){

                    notationValues["${String(nextNotation.toCharArray())}${cellPos+1}"] = cell.value
                    cell.cellNotation = "${String(nextNotation.toCharArray())}${cellPos+1}"
                    columnSymbol(nextNotation)

                } else if(cell.formula != null) {

                    notationValues["${String(nextNotation.toCharArray())}${cellPos+1}"] = null
                    cell.cellNotation = "${String(nextNotation.toCharArray())}${cellPos+1}"
                    columnSymbol(nextNotation)

                } else cell.error = "Invalid job"
            }
        }
    }

    fun evaluateJob() {
        setNotation()
        for (row in data){
            for(cell in row){
                if(cell.formula != null){
                    evaluateFormula(cell)
                    cell.formula = null
                }
            }
        }
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun evaluateFormula(cell: Data) {
        if (cell.formula!!.reference != null){
            reference(cell, cell.formula!!.reference!!)
        }
        else if(cell.formula?.sum != null){
            sum(cell, cell.formula!!.sum!!)
        }
        else if(cell.formula?.divide != null){
            division(cell, cell.formula!!.divide!!)
        }
        else if(cell.formula?.multiply != null){
            multiply(cell, cell.formula!!.multiply!!)
        }
        else if(cell.formula?.is_greater != null){
            isGreater(cell, cell.formula!!.is_greater!!)
        }
        else if(cell.formula?.is_equal != null){
            isEqual(cell, cell.formula!!.is_equal!!)
        }
        else if(cell.formula?.not != null){
            not(cell, cell.formula!!.not!!)
        }
        else if(cell.formula?.and != null){
            and(cell, cell.formula!!.and!!)
        }
        else if(cell.formula?.or != null){
            or(cell, cell.formula!!.or!!)
        }
        else if(cell.formula?.`if` != null){
            `if`(cell, cell.formula!!.`if`!!)
        }
        else if(cell.formula?.concat != null){
            concat(cell, cell.formula!!.concat!!)
        }
        else if(cell.formula?.isLess != null){
            isLess(cell, cell.formula!!.isLess!!)
        }
        else cell.error = "Invalid formula"

    }



    private fun validateForNumberValues(refList: List<Reference>): MutableList<Float> {
        val valueList: MutableList<Float> = ArrayList()
        for (ref in refList){
            if (ref.reference != null){
                if(!notationValues.containsKey(ref.reference)){
                    throw Exception("Invalid reference")
                } else if(notationValues[ref.reference]?.number == null) {
                    throw Exception("Invalid data type")
                } else {
                    valueList.add(notationValues[ref.reference]?.number!!)
                }
            } else if(ref.value != null) {
                if(ref.value.number == null){
                    throw Exception("Invalid data type")
                } else {
                    valueList.add(ref.value.number!!)
                }
            }
        }
        return valueList
    }

    private fun isLess(cell: Data, refList: List<Reference>) {
        if(refList.size != 2){
            cell.error = "Not enough values are given"
            return
        }
        try {
            val valueList : MutableList<Float> = validateForNumberValues(refList)
            cell.value = Value(boolean = valueList[0] < valueList[1])
            notationValues[cell.cellNotation] = cell.value
        } catch (e :Exception){
            cell.error = e.message
        }
    }

    //-------------------------------------------------------------------------------------------------------------//
    private fun concat(cell: Data, refList: List<Reference>) {
        var concatString = ""
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(ref in refList) {
            if(ref.reference != null) {
                if (!notationValues.containsKey(ref.reference)) {
                    cell.error = "Invalid reference"
                    return
                } else if (notationValues[ref.reference]?.text == null) {
                    cell.error = "Invalid data type"
                    return
                } else {
                    concatString += notationValues[ref.reference]?.text
                }
            } else {
                if (ref.value?.text == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else concatString += ref.value.text
            }
        }
        cell.value = Value(text = concatString)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun `if`(cell: Data, list: List<Formula>) {
        val condition = list[0]
            if(condition.is_equal != null){
                isEqual(cell,condition.is_equal)
                if(cell.value?.boolean!!){
                    cell.value = notationValues[condition.is_equal[0].reference]
                } else cell.value = notationValues[condition.is_equal[1].reference]
            }
            else if(condition.is_greater != null){
                isGreater(cell,condition.is_greater)
                print(cell)
                if(cell.value?.boolean!!){
                    cell.value = notationValues[condition.is_greater[0].reference]
                } else cell.value = notationValues[condition.is_greater[1].reference]
            }
            else if(condition.and != null){
                and(cell, condition.and)
                if(cell.value?.boolean!!){
                    cell.value = notationValues[condition.and[0].reference]
                } else cell.value = notationValues[condition.and[0].reference]
            }
            else if(condition.or != null){
                or(cell, condition.or)
                if(cell.value?.boolean!!){
                    cell.value = notationValues[condition.or[0].reference]
                } else cell.value = notationValues[condition.or[1].reference]
            }
            else if(condition.not != null){
                not(cell, condition.not)
                if(cell.value?.boolean!!){
                    cell.value = notationValues[condition.not.reference]
                }
            }
            else cell.error = "Invalid formula"
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun or(cell: Data, refList: List<Reference>) {
        val boolList = ArrayList<Boolean>()
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(ref in refList){
            if(ref.reference != null){
                if(!notationValues.containsKey(ref.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if(notationValues[ref.reference]?.boolean == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else boolList.add(notationValues[ref.reference]?.boolean!!)
            }
            else {
                if(ref.value?.boolean == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else boolList.add(ref.value.boolean!!)
            }
        }
        for(value in boolList){
            if (value){
                cell.value = Value(boolean = true)
                notationValues[cell.cellNotation] = cell.value
                return
            }
        }
        cell.value = Value(boolean = false)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun and(cell: Data, refList: List<Reference>) {
        val boolList = ArrayList<Boolean>()
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(ref in refList){
            if(ref.reference != null){
                if(!notationValues.containsKey(ref.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if(notationValues[ref.reference]?.boolean == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else boolList.add(notationValues[ref.reference]?.boolean!!)
            }
            else {
                if(ref.value?.boolean == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else boolList.add(ref.value.boolean!!)
            }
        }
        for(value in boolList){
            if (!value){
                cell.value = Value(boolean = false)
                notationValues[cell.cellNotation] = cell.value
                return
            }
        }
        cell.value = Value(boolean = true)
        notationValues[cell.cellNotation] = cell.value
    }

    //-------------------------------------------------------------------------------------------------------------//
    private fun not(cell: Data, ref: Reference) {
        val opposite: Boolean?
        if(ref.reference != null){
            if(!notationValues.containsKey(ref.reference)) {
                cell.error = "Invalid Reference"
                return
            }
            else if(notationValues[ref.reference]?.boolean == null) {
                cell.error = "Invalid data type"
                return
            }
            else opposite = notationValues[ref.reference]?.boolean!!.not()
        }
        else {
            if(ref.value?.boolean == null) {
                cell.error = "Invalid data type"
                return
            }
            else opposite = ref.value.boolean!!.not()
        }
        cell.value = Value(boolean = opposite)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun isEqual(cell: Data, refList: List<Reference>) {
        val valueList = ArrayList<Value>()
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(reference in refList) {
            if (notationValues[reference.reference] != null) {
                if (!notationValues.containsKey(reference.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if (notationValues[reference.reference] == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else valueList.add(notationValues[reference.reference]!!)
            } else {
                if (reference.value == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else valueList.add(reference.value)
            }
        }
        cell.value = Value(boolean = valueList[0] == valueList[1])
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun isGreater(cell: Data, refList: List<Reference>) {
        val floatList = ArrayList<Float>()
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(reference in refList) {
            if (notationValues[reference.reference] != null) {
                if (!notationValues.containsKey(reference.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if (notationValues[reference.reference]?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else floatList.add(notationValues[reference.reference]?.number!!)
            } else {
                if (reference.value?.boolean == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else floatList.add(reference.value.number!!)
            }
        }
        cell.value = Value(boolean = floatList[0] > floatList[1])
        notationValues[cell.cellNotation] = cell.value
    }

    //-------------------------------------------------------------------------------------------------------------//
    private fun multiply(cell: Data, refList: List<Reference>) {
        var operand = 1.0f
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(reference in refList){
            if(reference.reference != null) {
                if (!notationValues.containsKey(reference.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if (notationValues[reference.reference]?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else
                    operand *= notationValues[reference.reference]?.number!!
            }
            else {
                if(reference.value?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else operand *= reference.value.number!!
            }
        }
        cell.value = Value(number = operand)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun division(cell: Data, refList: List<Reference>) {
        var operand = 0.0f
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for((index,value) in refList.withIndex()){
            if(value.reference != null) {
                if (!notationValues.containsKey(value.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if (notationValues[value.reference]?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else if(index == 0) operand = notationValues[value.reference]?.number!!
                else if (notationValues[value.reference]?.number == 0.0f) {
                    cell.error = "Division by zero is not possible"
                    return
                }
                else operand /= notationValues[value.reference]?.number!!
            }
            else {
                if(value.value?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else if(index == 0) operand = value.value.number!!
                if(value.value.number == 0.0f) {
                    cell.error = "Division by zero is not possible"
                    return
                }
                else operand /= value.value.number!!
            }
        }
        cell.value = Value(number = (operand * 10000000.0f).roundToInt().toFloat()/10000000.0f)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun sum(cell: Data, refList: List<Reference>){
        var operand = 0.0f
        if(isGreaterThanN(refList,2)){
            cell.error = "Not enough values are given"
            return
        }
        for(reference in refList){
            if(reference.reference != null) {
                if (!notationValues.containsKey(reference.reference)) {
                    cell.error = "Invalid Reference"
                    return
                }
                else if (notationValues[reference.reference]?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else
                operand += notationValues[reference.reference]?.number!!
            }
            else {
                if(reference.value?.number == null) {
                    cell.error = "Invalid data type"
                    return
                }
                else operand += reference.value.number!!
            }
        }
        cell.value = Value(number = operand)
        notationValues[cell.cellNotation] = cell.value
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun reference(cell: Data, reference: String){
        if(notationValues.containsKey(reference)) {
            if(notationValues[reference] == null){
                if(notationValues[cell.cellNotation] == null){
                    notationValues[reference] = Value(number = 0.0f, text = "null")
                    notationValues[cell.cellNotation] = notationValues[reference]
                }
                else {
                    notationValues[reference] = notationValues[cell.cellNotation]
                }
            }
            else {
                if(notationValues[cell.cellNotation] != null) {
                    notationValues[cell.cellNotation]?.apply {
                        text = notationValues[reference]?.text
                        number = notationValues[reference]?.number
                        boolean = notationValues[reference]?.boolean
                    }
                }
                else  {
                    notationValues[cell.cellNotation] = notationValues[reference]
                }
            }
        } else {
            cell.error = "Invalid Reference"
            return
        }
        cell.value = notationValues[reference]
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun columnSymbol(A1: MutableList<Char>): MutableList<Char>{
        var i = 1
        while(A1[A1.size-i] == 'Z'){
            if(A1.size-i == 0) break
            A1[A1.size-i] = 'A'
            i += 1
        }
        if(A1[A1.size-i] == 'Z'){
            A1[A1.size-i] = 'A'
            A1.add('A')
        }
        else A1[A1.size-i] = (A1[A1.size-i].toInt() + 1).toChar()
        return A1
    }
    //-------------------------------------------------------------------------------------------------------------//
    private fun isGreaterThanN(refList: List<Reference>, n: Int): Boolean{
        return refList.size < n
    }
}