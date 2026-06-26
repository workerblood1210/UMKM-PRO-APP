package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val type: String,
    val packageName: String,
    val packageDesc: String,
    val imageUris: String = "",
    val bahanBakuIds: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bahan_baku")
data class BahanBaku(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Dao
interface BahanBakuDao {
    @Query("SELECT * FROM bahan_baku ORDER BY timestamp DESC")
    fun getAllBahanBaku(): Flow<List<BahanBaku>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBahanBaku(bahanBaku: BahanBaku)

    @Delete
    suspend fun deleteBahanBaku(bahanBaku: BahanBaku)
}

@Database(entities = [Product::class, BahanBaku::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun bahanBakuDao(): BahanBakuDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

class ProductRepository(private val productDao: ProductDao, private val bahanBakuDao: BahanBakuDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allBahanBaku: Flow<List<BahanBaku>> = bahanBakuDao.getAllBahanBaku()

    suspend fun insert(product: Product) = productDao.insertProduct(product)
    suspend fun delete(product: Product) = productDao.deleteProduct(product)
    
    suspend fun insertBahanBaku(bahanBaku: BahanBaku) = bahanBakuDao.insertBahanBaku(bahanBaku)
    suspend fun deleteBahanBaku(bahanBaku: BahanBaku) = bahanBakuDao.deleteBahanBaku(bahanBaku)
}
