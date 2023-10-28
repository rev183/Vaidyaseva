package com.mrknti.vaidyaseva.data

import java.util.Locale

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

object ServiceStatus {
    const val RAISED = "RAISED"
    const val COMPLETED = "COMPLETED"
}

object UserDocumentType {
    const val PASSPORT = 0
    const val VISA = 1
}

object HttpCodes {
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}

val LOCALE_IN: Locale
    get() = Locale("en", "IN")