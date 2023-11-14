package com.mrknti.vaidyaseva.data

import java.util.Locale

const val HOST_URL = "http://35.207.46.206:8080/"

object UserRoles {
    const val CLIENT = "CLIENT"
    const val ADMIN = "ADMIN"
    const val TRANSPORT = "TRANSPORT"
    const val HOUSEKEEPING = "HOUSEKEEPING"
    const val MANAGER = "MANAGER"
    const val SUPER_USER = "SUPER_USER"

    val CREATABLE_ROLES = listOf(
        Pair(CLIENT, "Client"),
        Pair(TRANSPORT, "Transport"),
        Pair(HOUSEKEEPING, "Housekeeping"),
        Pair(MANAGER, "Manager")
    )
}

enum class UserRoleUI(val value: String) {
    CLIENT("CLIENT"),
    ADMIN("ADMIN"),
    TRANSPORT("TRANSPORT"),
    HOUSEKEEPING("HOUSEKEEPING"),
    MANAGER("MANAGER"),
    SUPER_USER("SUPER_USER");

    val uiString: String
        get() = when (this) {
            CLIENT -> "Client"
            ADMIN -> "Admin"
            TRANSPORT -> "Transport"
            HOUSEKEEPING -> "Housekeeping"
            MANAGER -> "Manager"
            SUPER_USER -> "Super User"
        }

    companion object {
        fun getByValue(value: String): UserRoleUI {
            return when (value) {
                "CLIENT" -> CLIENT
                "ADMIN" -> ADMIN
                "TRANSPORT" -> TRANSPORT
                "HOUSEKEEPING" -> HOUSEKEEPING
                "MANAGER" -> MANAGER
                "SUPER_USER" -> SUPER_USER
                else -> CLIENT
            }
        }
    }
}

object ServiceType {
    const val CAB = "CAB"
    const val CLEANING = "CLEANING"
    const val PLUMBING = "PLUMBING"
    const val MEDICINE = "MEDICINE"
    const val NORMAL = "NORMAL"
    const val ROOM_SERVICE = "ROOM_SERVICE"
}

enum class ServiceTypeUI(val value: String) {
    CAB("CAB"),
    CLEANING("CLEANING"),
    PLUMBING("PLUMBING"),
    MEDICINE("MEDICINE"),
    NORMAL("NORMAL"),
    ROOM_SERVICE("ROOM_SERVICE");

    val uiString: String
        get() = when (this) {
            CAB -> "Cab"
            CLEANING -> "Cleaning"
            PLUMBING -> "Plumbing"
            MEDICINE -> "Medicine"
            NORMAL -> "General"
            ROOM_SERVICE -> "Room Service"
        }

    fun canBeAssignedTo(role: String): Boolean {
        if (role == UserRoles.SUPER_USER) return true
        return when (this) {
            CAB -> role == UserRoles.TRANSPORT
            CLEANING -> role == UserRoles.HOUSEKEEPING
            PLUMBING -> role == UserRoles.HOUSEKEEPING
            MEDICINE -> role == UserRoles.HOUSEKEEPING
            NORMAL -> role == UserRoles.MANAGER
            ROOM_SERVICE -> role == UserRoles.MANAGER
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

object HttpCodes {
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}

val LOCALE_IN: Locale
    get() = Locale("en", "IN")