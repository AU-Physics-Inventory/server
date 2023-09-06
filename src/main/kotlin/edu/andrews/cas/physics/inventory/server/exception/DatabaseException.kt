package edu.andrews.cas.physics.inventory.server.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR, reason = "An error occurred on the database.")
class DatabaseException : RuntimeException()
