package ir.ehsannarmani.calendar.utils

fun <T> List<T>.restOfFrom(from: T): List<T> {
    val fromIndex = indexOf(from)
    val result = mutableListOf<T>()
    for (i in fromIndex..lastIndex) {
        result.add(this[i])
    }
    for (i in 0 until fromIndex) {
        result.add(this[i])
    }
    return result
}

fun <T>List<T>.getNext(current:T):T{
    val currentIndex = indexOf(current)
    return if (currentIndex == lastIndex){
        first()
    }else{
        this[currentIndex+1]
    }
}