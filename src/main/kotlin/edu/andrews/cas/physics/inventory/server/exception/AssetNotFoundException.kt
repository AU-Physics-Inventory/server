package edu.andrews.cas.physics.inventory.server.exception

import org.bson.types.ObjectId

class AssetNotFoundException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(id: ObjectId) : super(id.toString())
}