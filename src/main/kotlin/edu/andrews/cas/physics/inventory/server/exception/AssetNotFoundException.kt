package edu.andrews.cas.physics.inventory.server.exception

import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND, reason = "The asset was not found.")
class AssetNotFoundException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(id: ObjectId) : super(id.toString())
}