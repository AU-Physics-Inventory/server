package edu.andrews.cas.physics.inventory.server.service.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import edu.andrews.cas.physics.inventory.server.dao.app.AssetDAO;
import edu.andrews.cas.physics.inventory.server.dao.app.ManualsDAO;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.set;

@Service
public class FileStorageService {
    private static final Logger logger = LogManager.getLogger();
    private final AmazonS3 spaces;
    private final AssetDAO assetDAO;
    private final ManualsDAO manualsDAO;
    private final String spaceName;

    @Autowired
    public FileStorageService(AmazonS3 spaces, AssetDAO assetDAO, ManualsDAO manualsDAO, @Qualifier("configProperties") Properties config) {
        this.spaces = spaces;
        this.assetDAO = assetDAO;
        this.manualsDAO = manualsDAO;
        this.spaceName = config.getProperty("spaces.name");

        logger.warn(System.getProperty("javax.net.ssl.keyStore"));
        logger.warn(System.getProperty("javax.net.ssl.trustStore"));
    }

    public String storeImage(@NonNull ObjectId id, @NonNull MultipartFile file) throws IOException, SdkClientException {
        logger.info("Storing image for asset with id '{}'", id.toString());
        String fileName = storeFile("images", file);
        Bson bson = addToSet("images", fileName);
        assetDAO.update(id, bson);
        return fileName;
    }

    private String storeFile(@NonNull String folder, @NonNull MultipartFile file) throws IOException, SdkClientException {
        String fileName;

        try {
            fileName = generateFileName(folder);
        } catch (SdkClientException e) {
            logger.error("An S3 error occurred", e);
            throw e;
        }

        File tempFile = File.createTempFile(fileName, null);
        try {
            logger.info("Transferring image to temporary file...");
            file.transferTo(tempFile);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            String bucketName = String.format("%s/%s", spaceName, folder);
            logger.info("Uploading file to S3 bucket - DigitalOcean Spaces - at bucket {}", bucketName);
            spaces.putObject(new PutObjectRequest(bucketName, fileName, tempFile).withCannedAcl(CannedAccessControlList.PublicRead).withMetadata(metadata));
        } catch (IOException e) {
            logger.error("Error creating temporary file", e);
            throw e;
        } catch (SdkClientException e) {
            logger.error("Error uploading file to S3 bucket", e);
            throw e;
        } finally {
            tempFile.delete();
        }

        return fileName;
    }

    private String generateFileName(@NonNull String folder) throws SdkClientException {
        logger.info("Generating file name...");
        String fileName;
        do {
            fileName = RandomStringUtils.randomAlphanumeric(8, 20);
            logger.info("Checking if file name '{}' is valid...", fileName);
        } while (this.spaces.doesObjectExist(String.format("%s/%s", this.spaceName, folder), fileName));
        logger.info("Found valid file name: {}", fileName);
        return fileName;
    }

    public String storeManual(@NonNull Integer identityNo, @NonNull MultipartFile file) throws IOException, SdkClientException {
        logger.info("Storing manual for IdentityNo '{}'", identityNo.toString());
        String fileName = storeFile("manuals", file);
        Bson bson = set("softcopy", fileName);
        manualsDAO.update(identityNo, bson);
        return fileName;
    }
}

