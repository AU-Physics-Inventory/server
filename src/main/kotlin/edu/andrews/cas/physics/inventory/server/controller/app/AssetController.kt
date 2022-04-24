package edu.andrews.cas.physics.inventory.server.controller.app

import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.service.app.AssetService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/app/assets")
class AssetController @Autowired constructor(private val assetService: AssetService) {

    @GetMapping("/asset")
    fun getAsset(@RequestParam id: String) : ResponseEntity<Asset> {
        logger.info("[Asset Controller] Received request for asset with id = '{}'", id)
        val asset = assetService.getAsset(id)
        return ResponseEntity.ok(asset)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}