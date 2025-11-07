package com.havrebollsolutions.ttpoademoapp.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "config")
data class Config(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Nonnull @ColumnInfo(name = "config_id") val configId: String
)
