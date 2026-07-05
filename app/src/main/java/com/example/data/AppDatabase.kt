package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromAccountStatus(value: AccountStatus): String = value.name
    @TypeConverter
    fun toAccountStatus(value: String): AccountStatus = AccountStatus.valueOf(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromKeyStatus(value: KeyStatus): String = value.name
    @TypeConverter
    fun toKeyStatus(value: String): KeyStatus = KeyStatus.valueOf(value)

    @TypeConverter
    fun fromTrustLevel(value: TrustLevel): String = value.name
    @TypeConverter
    fun toTrustLevel(value: String): TrustLevel = TrustLevel.valueOf(value)

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus): String = value.name
    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus = SessionStatus.valueOf(value)

    @TypeConverter
    fun fromChallengeStatus(value: ChallengeStatus): String = value.name
    @TypeConverter
    fun toChallengeStatus(value: String): ChallengeStatus = ChallengeStatus.valueOf(value)

    @TypeConverter
    fun fromAuditAction(value: AuditAction): String = value.name
    @TypeConverter
    fun toAuditAction(value: String): AuditAction = AuditAction.valueOf(value)
}

@Database(
    entities = [
        User::class,
        PublicKeyEntity::class,
        Session::class,
        AuditLog::class,
        Challenge::class,
        ClientKey::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aardvark_identity_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
