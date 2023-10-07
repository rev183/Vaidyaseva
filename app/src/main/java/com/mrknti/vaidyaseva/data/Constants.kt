package com.mrknti.vaidyaseva.data

import java.util.Locale

object UserRoles {
    const val CLIENT = "CLIENT"
    const val SERVICE_USER = "SERVICE_USER"
    const val TRANSPORT = "TRANSPORT"
    const val HOUSEKEEPING = "HOUSEKEEPING"
    const val SUPER_USER = "SUPER_USER"
}

object ServiceType {
    const val CAB = "CAB"
    const val CAB_PICKUP = "CAB_PICKUP"
    const val CAB_DROP = "CAB_DROP"
    const val CLEANING = "CLEANING"
    const val PLUMBING = "PLUMBING"
    const val MEDICINE = "MEDICINE"
    const val NORMAL = "NORMAL"
    const val ROOM_SERVICE = "ROOM_SERVICE"
}

object ServiceStatus {
    const val RAISED = "RAISED"
    const val COMPLETED = "COMPLETED"
}

val LOCALE_IN: Locale
    get() = Locale("en", "IN")