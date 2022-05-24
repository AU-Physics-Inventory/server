package edu.andrews.cas.physics.inventory.server.controller.app

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.request.app.NewAssetRequest
import edu.andrews.cas.physics.inventory.server.request.app.UpdateAssetRequest
import edu.andrews.cas.physics.inventory.server.response.app.InsertedAssetResponse
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

    @PostMapping("/asset")
    fun add(
        @RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken,
        @RequestBody newAssetRequest: NewAssetRequest,
    ): ResponseEntity<InsertedAssetResponse> {
        logger.info("[Asset Controller] Received request to add asset: {}", newAssetRequest)
        val result = assetService.addAsset(newAssetRequest, jwt)
        return ResponseEntity.status(201).body(InsertedAssetResponse(result.first.toString(), result.second))
    }

    @DeleteMapping("/asset")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken,
               @RequestParam id: String) : ResponseEntity<Any> {
        logger.info("[Asset Controller] Received request to delete asset {}", id)
        return if (assetService.deleteAsset(id, jwt)) ResponseEntity.accepted().build()
        else ResponseEntity.status(401).build()
    }

    @PutMapping("/asset")
    fun update(
        @RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken,
        @RequestBody updateAssetRequest: UpdateAssetRequest
    ): ResponseEntity<Any> {
        logger.info("[Asset Controller] Received request to update asset: {}")
        val objectID = assetService.updateAsset(updateAssetRequest, jwt)
        return if (objectID == null) ResponseEntity.ok().build() else ResponseEntity.accepted()
            .body(objectID.toHexString())
    }

    @PostMapping("/asset/keywords")
    fun addKeywords(
        @RequestParam id: String,
        @RequestBody keywords: List<String>
    ): ResponseEntity<Any> {
        logger.info("[Asset Controller] Received request to add keywords to asset '{}': {}", id, keywords)
        assetService.addKeywords(id, keywords)
        return ResponseEntity.accepted().build()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}