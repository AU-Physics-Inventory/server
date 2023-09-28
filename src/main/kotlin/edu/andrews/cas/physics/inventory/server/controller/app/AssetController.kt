package edu.andrews.cas.physics.inventory.server.controller.app

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.exception.InvalidAssetRequestException
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.request.app.asset.NewAssetRequest
import edu.andrews.cas.physics.inventory.server.request.app.asset.PatchKeywordsRequest
import edu.andrews.cas.physics.inventory.server.request.app.asset.UpdateAssetRequest
import edu.andrews.cas.physics.inventory.server.response.app.InsertedAssetResponse
import edu.andrews.cas.physics.inventory.server.service.app.AssetService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
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
        logger.info("[Asset Controller] Received request to update asset: {}", updateAssetRequest)
        val objectID = assetService.updateAsset(updateAssetRequest, jwt)
        return if (objectID == null) ResponseEntity.ok().build() else ResponseEntity.accepted()
            .body(objectID.toHexString())
    }

    @PatchMapping("/asset/keywords")
    fun patchKeywords(@RequestBody request: PatchKeywordsRequest): ResponseEntity<Any> {
        logger.info("[Asset Controller] Received request to patch keywords: {}", request)
        if (request.assetID.isNullOrBlank()) throw InvalidAssetRequestException("id")
        if (request.keywords.isNullOrEmpty()) throw InvalidAssetRequestException("keywords")
        try {
            val id = ObjectId(request.assetID)
            when (request.patchOption) {
                PatchKeywordsRequest.Option.ADD -> assetService.addKeywords(id, request.keywords)
                PatchKeywordsRequest.Option.REMOVE -> assetService.deleteKeywords(id, request.keywords)
                null -> throw InvalidAssetRequestException("patchOption")
            }
            return ResponseEntity.accepted().build()
        } catch (e: IllegalArgumentException) {
            logger.error("Unable to parse objectId '{}'", request.assetID)
            logger.error(e)
            throw InvalidAssetRequestException("id")
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}