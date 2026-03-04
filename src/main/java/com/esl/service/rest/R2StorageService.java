package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Service
public class R2StorageService {
    private static final Logger logger = LoggerFactory.getLogger(R2StorageService.class);

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public R2StorageService(
            @Value("${R2_ENDPOINT:}") String endpoint,
            @Value("${R2_BUCKET:}") String bucket,
            @Value("${R2_ACCESS_KEY_ID:}") String accessKeyId,
            @Value("${R2_SECRET_ACCESS_KEY:}") String secretAccessKey,
            @Value("${R2_PUBLIC_BASE_URL:}") String publicBaseUrl
    ) {
        if (StringUtils.isBlank(endpoint) || StringUtils.isBlank(bucket)
                || StringUtils.isBlank(accessKeyId) || StringUtils.isBlank(secretAccessKey)) {
            logger.warn("R2 config missing; R2 uploads will be disabled");
            this.s3Client = null;
            this.bucket = null;
            this.publicBaseUrl = null;
            return;
        }

        this.bucket = bucket.trim();
        this.publicBaseUrl = StringUtils.trimToNull(publicBaseUrl);

        String normalizedEndpoint = endpoint.trim();
        String bucketSuffix = "/" + this.bucket;
        if (normalizedEndpoint.endsWith(bucketSuffix)) {
            normalizedEndpoint = normalizedEndpoint.substring(0, normalizedEndpoint.length() - bucketSuffix.length());
        }

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(normalizedEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId.trim(), secretAccessKey.trim())
                ))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    public boolean isConfigured() {
        return s3Client != null;
    }

    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    public void putBytes(String key, byte[] data, String contentType) {
        var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);
        if (StringUtils.isNotBlank(contentType)) {
            request.contentType(contentType);
        }
        s3Client.putObject(request.build(), RequestBody.fromBytes(data));
        logger.info(
                "Stored object in R2 bucket={} key={} bytes={} contentType={} publicUrl={}",
                bucket,
                key,
                data == null ? 0 : data.length,
                StringUtils.defaultIfBlank(contentType, "unknown"),
                StringUtils.defaultIfBlank(buildPublicUrl(key), "n/a")
        );
    }

    public String buildPublicUrl(String key) {
        if (StringUtils.isBlank(publicBaseUrl)) {
            return null;
        }
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return base + "/" + key;
    }
}
