import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

//เมื่อมีคนโอนเงิน แอปจะสร้างtransaction(ใบเสร็จ ) ส่งใบเส็จให้ Dao(พนักงาน) พนักงานจะเอาไปเก็บใน appDatabase(ตู้เก็บ)
@Database(entities = [Transaction::class], version = 1) //database transaction v.1
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
