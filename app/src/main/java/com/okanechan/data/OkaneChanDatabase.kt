package com.okanechan.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date


@Entity
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @TypeConverters(DateConverter::class) val date: Date,
    val totalAmount: Double,
    val shopName: String,
    val notes: String = ""
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["expenseId"])]
)
data class ExpensePartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val expenseId: Int,
    val amount: Double,
    val description: String,
)

@Entity
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String
)

@Entity(
    primaryKeys = ["expensePartId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpensePartEntity::class,
            parentColumns = ["id"],
            childColumns = ["expensePartId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["tagId"])]
)
data class ExpensePartAndTagCrossRef(
    val expensePartId: Int,
    val tagId: Int
)

data class ExpensePartWithTagsRelation(
    @Embedded
    val expensePart: ExpensePartEntity,
    @Relation(
        entity = TagEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ExpensePartAndTagCrossRef::class,
            parentColumn = "expensePartId",
            entityColumn = "tagId"
        )
    )
    val tags: Set<TagEntity>
)

data class ExpenseWithPartsAndTagsRelation(
    @Embedded
    val expense: ExpenseEntity,
    @Relation(entity = ExpensePartEntity::class, parentColumn = "id", entityColumn = "expenseId")
    val partsWithTags: List<ExpensePartWithTagsRelation>
)

@Dao
interface OkaneChanDao {
    @Insert
    fun insert(expense: ExpenseEntity)

    @Insert
    fun insert(expense: List<ExpenseEntity>)

    @Insert
    fun insert(expensePart: ExpensePartEntity)

    @Insert
    fun insert(tag: TagEntity)

    @Insert
    fun insert(expensePartAndTag: ExpensePartAndTagCrossRef)

    @Query("SELECT * FROM ExpenseEntity")
    fun getExpenses(): List<ExpenseEntity>

    @Transaction
    @Query("SELECT * FROM ExpenseEntity")
    fun getExpensesWithPartsAndTags(): List<ExpenseWithPartsAndTagsRelation>

    @Transaction
    @Query("SELECT * FROM ExpensePartEntity WHERE id = :id")
    fun getExpensePart(id: Int): ExpensePartWithTagsRelation?
}

@Database(
    entities = [ExpenseEntity::class, ExpensePartEntity::class, TagEntity::class, ExpensePartAndTagCrossRef::class],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class OkaneChanDatabase : RoomDatabase() {
    abstract fun okaneChanDao(): OkaneChanDao
}

object DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long): Date = dateLong.let { Date(it) }

    @TypeConverter
    fun fromDate(date: Date): Long = date.time
}