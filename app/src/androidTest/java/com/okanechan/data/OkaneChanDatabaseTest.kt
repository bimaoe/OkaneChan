package com.okanechan.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat
import android.database.sqlite.SQLiteConstraintException

@RunWith(AndroidJUnit4::class)
class OkaneChanDatabaseTest {
    private lateinit var okaneChanDao: OkaneChanDao
    private lateinit var db: OkaneChanDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, OkaneChanDatabase::class.java
        ).build()
        okaneChanDao = db.okaneChanDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun getExpenses_expenseIsReturnedCorrectly() {
        // Arrange
        val expense = ExpenseEntity(
            id = 1,
            date = SimpleDateFormat("yyyy-MM-dd").parse("2024-01-09")!!,
            totalAmount = 1.00,
            shopName = "Susureti"
        )
        okaneChanDao.insert(expense)

        // Act
        val expenses = okaneChanDao.getExpenses()

        // Assert
        assertEquals(1, expenses.size)
        assertEquals(expense, expenses.single())
    }

    @Test
    fun insert_twoExpensesWithTheSameId_throws() {
        // Arrange
        val expense1 = ExpenseEntity(
            id = 1,
            date = SimpleDateFormat("yyyy-MM-dd").parse("2024-01-09")!!,
            totalAmount = 1.00,
            shopName = "Susureti"
        )

        val expense2 = ExpenseEntity(
            id = 1,
            date = SimpleDateFormat("yyyy-MM-dd").parse("2024-01-09")!!,
            totalAmount = 1.00,
            shopName = "Susureti"
        )

        // Act & Assert
        assertThrows(
            SQLiteConstraintException::class.java
        ) { okaneChanDao.insert(listOf(expense1, expense2)) }
    }

    @Test
    fun getExpensesWithPartsAndTags_expenseIsReturnedCorrectly() {
        val expense = ExpenseEntity(
            id = 1,
            date = SimpleDateFormat("yyyy-MM-dd").parse("2024-01-09")!!,
            totalAmount = 1.00,
            shopName = "Susureti"
        )
        val expensePart =
            ExpensePartEntity(id = 1, expenseId = 1, amount = 1.00, description = "Saurosu")
        val tag1 = TagEntity(id = 1, name = "Pet")
        val tag2 = TagEntity(id = 2, name = "Hobby")
        okaneChanDao.insert(expense)
        okaneChanDao.insert(expensePart)
        okaneChanDao.insert(tag1)
        okaneChanDao.insert(tag2)
        okaneChanDao.insert(ExpensePartAndTagCrossRef(expensePartId = 1, tagId = 1))
        okaneChanDao.insert(ExpensePartAndTagCrossRef(expensePartId = 1, tagId = 2))

        val expenseWithPart = ExpenseWithPartsAndTagsRelation(
            expense,
            listOf(ExpensePartWithTagsRelation(expensePart, setOf(tag1, tag2)))
        )

        // Act
        val expenses = okaneChanDao.getExpensesWithPartsAndTags()

        // Assert
        assertEquals(1, expenses.size)
        assertEquals(expenseWithPart, expenses.single())
    }

    @Test
    fun insert_expensePartWithNoLinkedExpense_throws() {
        val expensePart =
            ExpensePartEntity(id = 1, expenseId = 100, amount = 1.00, description = "Saurosu")

        assertThrows(
            SQLiteConstraintException::class.java
        ) { okaneChanDao.insert(expensePart) }
    }

    @Test
    fun getExpensesPart_expensePartIsReturnedCorrectly() {
        val expense = ExpenseEntity(
            id = 1,
            date = SimpleDateFormat("yyyy-MM-dd").parse("2024-01-09")!!,
            totalAmount = 1.00,
            shopName = "Susureti"
        )
        val expensePart =
            ExpensePartEntity(id = 1, expenseId = 1, amount = 1.00, description = "Saurosu")
        val wrongExpensePart =
            ExpensePartEntity(id = 2, expenseId = 1, amount = 1.00, description = "Saurosu")
        val tag1 = TagEntity(id = 1, name = "Pet")
        val tag2 = TagEntity(id = 2, name = "Hobby")
        okaneChanDao.insert(expense)
        okaneChanDao.insert(expensePart)
        okaneChanDao.insert(wrongExpensePart)
        okaneChanDao.insert(tag1)
        okaneChanDao.insert(tag2)
        okaneChanDao.insert(ExpensePartAndTagCrossRef(expensePartId = 1, tagId = 1))
        okaneChanDao.insert(ExpensePartAndTagCrossRef(expensePartId = 1, tagId = 2))

        val expensePartWithTag =
            ExpensePartWithTagsRelation(expensePart = expensePart, tags = setOf(tag1, tag2))

        // Act
        val expensePartResult = okaneChanDao.getExpensePart(id = 1)

        // Assert
        assertEquals(expensePartWithTag, expensePartResult)
    }

}