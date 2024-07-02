package com.example.appquizlet.model

import android.os.Parcel
import android.os.Parcelable

data class StreakData(
    val lastTime: Long,
    val currentStreak: Int
)

data class TaskData(
    val id: Int,
    val taskName: String,
    val type: String,
    val score: Int,
    val status: Int,
    val description: String?,
    val condition: Int,
    val progress: Int,
    val studied: Boolean? = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt() != 0
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(id)
        p0.writeString(taskName)
        p0.writeString(type)
        p0.writeInt(status)
        p0.writeString(description)
        p0.writeInt(condition)
        p0.writeInt(progress)
        p0.writeInt(if (studied == true) 1 else 0) // Convert boolean to int
    }

    companion object CREATOR : Parcelable.Creator<TaskData> {
        override fun createFromParcel(parcel: Parcel): TaskData {
            return TaskData(parcel)
        }

        override fun newArray(size: Int): Array<TaskData?> {
            return arrayOfNulls(size)
        }
    }
}

data class AchievementData(
    val version: Int,
    val specialName: String,
    val taskList: List<TaskData>
)

data class DetectContinueModel(
    val streak: StreakData,
    val achievement: AchievementData
)