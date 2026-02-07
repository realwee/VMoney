import androidx.room.TypeConverter

//ในroomไม่รู้จัก ype enum เลยต้องเขียนอันนนี้เพื่อแปลภาษา type enum
class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name // แปลง Enum เป็น String เพื่อเก็บในตู้
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value) // แปลง String จากตู้กลับมาเป็น Enum
    }
}