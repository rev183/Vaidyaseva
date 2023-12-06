package com.mrknti.vaidyaseva.data

import androidx.annotation.DrawableRes
import com.mrknti.vaidyaseva.R
import java.util.Locale

const val HOST_URL = "http://35.207.46.206:8080/"
const val APOLLO_HOSPITAL_ID = 36

enum class UserRole(val value: String) {
    CLIENT("CLIENT"),
    TRANSPORT("TRANSPORT"),
    HOUSEKEEPING("HOUSEKEEPING"),
    RECRUITER("RECRUITER"),
    MANAGER("MANAGER"),
    ADMIN("ADMIN");

    val uiString: String
        get() = when (this) {
            CLIENT -> "Client"
            TRANSPORT -> "Transport"
            HOUSEKEEPING -> "Housekeeping"
            RECRUITER -> "Recruiter"
            MANAGER -> "Manager"
            ADMIN -> "Admin"
        }



    companion object {
        fun getByValue(value: String): UserRole {
            return when (value) {
                "CLIENT" -> CLIENT
                "TRANSPORT" -> TRANSPORT
                "HOUSEKEEPING" -> HOUSEKEEPING
                "RECRUITER" -> RECRUITER
                "MANAGER" -> MANAGER
                "ADMIN" -> ADMIN
                else -> CLIENT
            }
        }

        fun creatableRolesByMe(role: List<String>): List<UserRole> {
            if (role.contains(ADMIN.value)) return listOf(
                CLIENT,
                TRANSPORT,
                HOUSEKEEPING,
                RECRUITER,
                MANAGER
            ) else if (role.contains(MANAGER.value)) return listOf(
                CLIENT,
                TRANSPORT,
                HOUSEKEEPING,
                RECRUITER,
            ) else if (role.contains(RECRUITER.value)) return listOf(
                CLIENT,
            ) else {
                return emptyList()
            }
        }
    }
}

enum class ServiceType(val value: String) {
    TRANSPORT("TRANSPORT"),
    VISA_RENEWAL("VISA_RENEWAL"),
    HOUSE_KEEPING("HOUSE_KEEPING"),
    NORMAL("NORMAL");

    val uiString: String
        get() = when (this) {
            TRANSPORT -> "Cab"
            VISA_RENEWAL -> "Visa Renewal"
            NORMAL -> "General"
            HOUSE_KEEPING -> "Room Service"
        }

    val needsRoom: Boolean
        get() = when (this) {
            TRANSPORT -> true
            VISA_RENEWAL -> false
            NORMAL -> false
            HOUSE_KEEPING -> true
        }

    @get:DrawableRes
    val iconRes: Int
        get() = when (this) {
            TRANSPORT -> R.drawable.local_taxi_24
            VISA_RENEWAL -> R.drawable.task_24
            NORMAL -> R.drawable.question_circle_24
            HOUSE_KEEPING -> R.drawable.concierge_24
        }

    fun canBeAssignedTo(role: String): Boolean {
        if (role == UserRole.ADMIN.value) return true
        return when (this) {
            TRANSPORT -> role == UserRole.TRANSPORT.value
            VISA_RENEWAL -> role == UserRole.MANAGER.value
            NORMAL -> role == UserRole.MANAGER.value
            HOUSE_KEEPING -> role == UserRole.HOUSEKEEPING.value
        }
    }

    companion object {
        fun getByValue(value: String): ServiceType {
            return when (value) {
                "TRANSPORT" -> TRANSPORT
                "VISA_RENEWAL" -> VISA_RENEWAL
                "HOUSE_KEEPING" -> HOUSE_KEEPING
                "NORMAL" -> NORMAL
                else -> NORMAL
            }
        }
    }
}

enum class OccupancyStatus(val value: Int) {
    BOOKED(0),
    CHECK_IN(1),
    CHECK_OUT(2),
    CANCELLED(3),
    UNKNOWN(-1);

    companion object {
        fun getByValue(value: Int): OccupancyStatus {
            return when (value) {
                0 -> BOOKED
                1 -> CHECK_IN
                2 -> CHECK_OUT
                3 -> CANCELLED
                else -> UNKNOWN
            }
        }
    }
}

object ServiceStatus {
    const val RAISED = "RAISED"
    const val COMPLETED = "COMPLETED"
}

object UserDocumentType {
    const val PASSPORT = 0
    const val VISA = 1
}

fun getDocumentUrl(id: Int) = "${HOST_URL}file/data?documentId=$id"
fun getBuildingGalleryUrl(id: Int) = "${HOST_URL}building-gallery/data?documentId=$id"

object HttpCodes {
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}

val LOCALE_IN: Locale
    get() = Locale("en", "IN")