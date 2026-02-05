import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions") //create table
data class Transaction(
    @PrimaryKey(autoGenerate = true) //การรันเลขprimarykey
    val id: Int = 0,

    val title: String,
    val amount: Double,//ทศนิยม
    val type: TransactionType, //income or expense
    val category: String,
    val date: Long, //Timestamp
    val note: String,
    val imagePath: String? = null,
)