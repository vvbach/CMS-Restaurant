package vn.tts.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**upload file lên minio**/
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        ObjectWriteResponse data = minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(dataString()+objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return data.object();
    }

    /**Trả về định dạng ddMMyyyy của ngày hiện tại*/
    private String dataString() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        return today.format(formatter);
    }

    /**Trả về đường dẫn để view file từ minio*/
    public String getPresignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET) // URL cho phép GET (download)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(20, TimeUnit.MINUTES) // Thời gian hiệu lực
                        .build()
        );
    }


}
