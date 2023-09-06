package edu.andrews.cas.physics.inventory.server.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN, reason = "User has exceeded maximum login attempts.")
class ExceededMaxLoginAttemptsException : RuntimeException()
