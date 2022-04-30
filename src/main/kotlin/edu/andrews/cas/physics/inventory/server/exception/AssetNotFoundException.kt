package edu.andrews.cas.physics.inventory.server.exception

class AssetNotFoundException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
}