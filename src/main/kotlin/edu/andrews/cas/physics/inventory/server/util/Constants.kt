package edu.andrews.cas.physics.inventory.server.util

object Constants {
    const val MIN_PASSWORD_LENGTH: Int = 8
    const val DEFAULT_TIMEOUT: Long = 1_800_000 // in milliseconds, equivalent to 30 minutes
    const val MAX_FAILED_LOGIN_ATTEMPTS = 3
}