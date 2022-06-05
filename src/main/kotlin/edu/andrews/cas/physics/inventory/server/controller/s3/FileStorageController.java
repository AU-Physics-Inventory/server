package edu.andrews.cas.physics.inventory.server.controller.s3;

import com.amazonaws.SdkClientException;
import edu.andrews.cas.physics.inventory.server.exception.InvalidAssetRequestException;
import edu.andrews.cas.physics.inventory.server.service.s3.FileStorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class FileStorageController {
    private static final Logger logger = LogManager.getLogger();
    private final FileStorageService fileStorageService;

    @Autowired
    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/app/assets/asset/images")
    public ResponseEntity<Object> uploadImage(@RequestParam String assetID, @RequestParam MultipartFile image) {
        logger.info("Received request to upload image file for asset with id '{}'", assetID);
        try {
            ObjectId id = new ObjectId(assetID);
            String fileName = fileStorageService.storeImage(id, image);
            return ResponseEntity.ok(fileName);
        } catch (IllegalArgumentException e) {
            logger.error("Unable to parse objectId '{}'", assetID);
            logger.error(e);
            throw new InvalidAssetRequestException("id");
        } catch (IOException e) {
            logger.error("Returning HTTP 500 - Internal Server Error");
            return ResponseEntity.internalServerError().body("An error occurred receiving the uploaded image.");
        } catch (SdkClientException e) {
            logger.error("Returning HTTP 500 - Internal Server Error");
            return ResponseEntity.internalServerError().body("An error occurred while communicating with the file storage service.");
        }
    }
}
