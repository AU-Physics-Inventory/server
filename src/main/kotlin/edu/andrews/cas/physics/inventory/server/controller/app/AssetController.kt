package edu.andrews.cas.physics.inventory.server.controller.app

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.request.app.AssetRequest
import edu.andrews.cas.physics.inventory.server.service.app.AssetService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/app/assets")
class AssetController @Autowired constructor(private val assetService: AssetService) {
    @GetMapping("/asset")
    fun get(@RequestParam id: String): ResponseEntity<Asset> {
        logger.info("[Asset Controller] Received request for asset with id = '{}'", id)
        val asset = assetService.getAssets(arrayListOf(id))
        return if (asset.isEmpty()) ResponseEntity.notFound().build()
        else ResponseEntity.ok(asset[0])
    }

    @GetMapping("/assets")
    fun get(@RequestParam ids: List<String>): ResponseEntity<List<Asset>> {
        logger.info("[Asset Controller] Received request for asset with id = '{}'", ids.toString())
        val assets = assetService.getAssets(ids)
        return ResponseEntity.ok(assets)
    }

    @GetMapping
    fun search(@RequestParam params: Map<String, String>): ResponseEntity<List<Asset>> {
        logger.info("[Asset Controller] Received search request: {}", params.toString())
        // todo catch number format exception
        val assets = assetService.search(params)
        return ResponseEntity.ok(assets)
    }

    @PostMapping("/add")
    fun add(
        @RequestBody assetRequest: AssetRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken
    ): ResponseEntity<Any> {
        logger.info("[Asset Controller] Received request to add asset: {}", assetRequest)
        val result = assetService.addAsset(assetRequest, jwt)
        return if (result == null) ResponseEntity.noContent().build() else ResponseEntity.status(204)
            .body(result.toString())
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}