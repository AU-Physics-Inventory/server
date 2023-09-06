package edu.andrews.cas.physics.inventory.server.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN, reason = "Account has been disabled.")
class AccountDisabledException : RuntimeException()
