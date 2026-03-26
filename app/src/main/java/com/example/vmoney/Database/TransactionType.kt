package com.example.vmoney.Database
enum class TransactionType {
    INCOME,
    EXPENSE
}
enum class TransactionCategory(val displayName : String) {
    PERSONAL("ส่วนตัว"),
    FOOD("อาหาร"),
    PRIMARY_STORE("ร้านปฐม"),
    SECONDFLOOR_STORE("ร้านชั้น2"),
    HOME_STORE("ร้านที่บ้าน"),
    INCOME("รายรับ"),
    OTHER("อื่นๆ")

}