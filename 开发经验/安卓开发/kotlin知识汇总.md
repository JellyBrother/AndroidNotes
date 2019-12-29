### 用?和?:基本上能避免程序中出现的所有NullPointerException
[具体参考](https://www.jianshu.com/p/51b2e5aa3dd8)
val room: Room? = Room()    // 先实例化一个room，并且room可以为空
val room: Room? = null  // 不实例化了，开始room就是空的
val room: Room = Room()   // 实例化一个room，并且room永远不能为空
val room = Room()   // 和上一行代码一样，是KT最常用的简写语法
Log.d("TAG", "-->> room name = ${room?.roomName}") // 因为在调用时加上了问号，所以程序不会抛出异常
Kotlin提供了对象A ?: 对象B表达式，并且取消了Java中的条件表达式 ? 表达式1 : 表达式2这个三元表达式。
val roomList: ArrayList<Room>? = null
if (roomList?.size ?: 0 > 0) {// 这一行添加了?:，如果不加，就会抛异常。当roomList为null的时，它的size返回就是"null"，"null"不可以和int值比大小。
    Log.d("TAG", "-->> 房间数不是0")
}

### Kotlin中when
[具体参考](https://www.jianshu.com/p/5960a52fe491)
* 其实kotlin中的when就是java的switch
enum class Anima { //kotlin的枚举要加关键字class
    DOG, CAT, BEAR
}
fun useWhen(anima: Anima) {
    when (anima) {
        Anima.BEAR -> println("when bear")
        Anima.DOG -> println("when dog")
        Anima.CAT -> println("when cat")
        else -> ""
    }
}































