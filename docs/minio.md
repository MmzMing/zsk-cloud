``` JAVA
package com.todoitbo.tallybookdasmart.utils;

import com.alibaba.fastjson2.JSON;
import com.todoitbo.tallybookdasmart.constant.BaseBoConstants;
import com.todoitbo.tallybookdasmart.exception.BusinessException;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author xiaobo
 * @date 2023/5/21
 */
@Component
public class MinioUtil {


    private static MinioClient minioClient;

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        MinioUtil.minioClient = minioClient;
    }

    /**
     * description: 文件上传
     *
     * @param bucketName 桶名称
     * @param file       文件
     * @param fileName   文件名
     * @author bo
     * @date 2023/5/21 13:06
     */
    public static String upload(String bucketName, MultipartFile file, String fileName) {
        // 返回客户端文件系统中的原始文件名
        String originalFilename = file.getOriginalFilename();
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .build());
            return bucketName + "/" + fileName;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败：", e.getMessage()).setCause(e).setLog();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * description: 文件删除
     *
     * @author bo
     * @date 2023/5/21 11:34
     */
    public static boolean delete(String bucketName, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName)
                    .object(fileName).build());
            return BaseBoConstants.TRUE;
        } catch (Exception e) {
            throw new BusinessException("Minio文件删除失败", e.getMessage()).setCause(e).setLog();
        }
    }

    /**
     * description: 删除桶
     *
     * @param bucketName 桶名称
     * @author bo
     * @date 2023/5/21 11:30
     */
    public static boolean removeBucket(String bucketName) {
        try {
            List<Object> folderList = getFolderList(bucketName);
            List<String> fileNames = new ArrayList<>();
            if (!folderList.isEmpty()) {
                for (Object value : folderList) {
                    Map o = (Map) value;
                    String name = (String) o.get("fileName");
                    fileNames.add(name);
                }
            }
            if (!fileNames.isEmpty()) {
                for (String fileName : fileNames) {
                    delete(bucketName, fileName);
                }
            }
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            return BaseBoConstants.TRUE;
        } catch (Exception e) {
            throw new BusinessException("Minio删除桶失败:", e.getMessage()).setCause(e).setLog();
        }
    }

    /**
     * description: 获取桶下所有文件的文件名+大小
     *
     * @param bucketName 桶名称
     * @author bo
     * @date 2023/5/21 11:39
     */
    public static List<Object> getFolderList(String bucketName) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        Iterator<Result<Item>> iterator = results.iterator();
        List<Object> items = new ArrayList<>();
        String format = "{'fileName':'%s','fileSize':'%s'}";
        while (iterator.hasNext()) {
            Item item = iterator.next().get();
            items.add(JSON.parse((String.format(format, item.objectName(),
                    formatFileSize(item.size())))));
        }
        return items;
    }

    /**
     * description: 格式化文件大小
     *
     * @param fileS 文件的字节长度
     * @author bo
     * @date 2023/5/21 11:40
     */
    private static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " GB";
        }
        return fileSizeString;
    }

    /**
     * 讲快文件合并到新桶   块文件必须满足 名字是 0 1  2 3 5....
     *
     * @param bucketName  存块文件的桶
     * @param bucketName1 存新文件的桶
     * @param fileName1   存到新桶中的文件名称
     * @return boolean
     */
    public static boolean merge(String bucketName, String bucketName1, String fileName1) {
        try {
            List<ComposeSource> sourceObjectList = new ArrayList<ComposeSource>();
            List<Object> folderList = getFolderList(bucketName);
            List<String> fileNames = new ArrayList<>();
            if (!folderList.isEmpty()) {
                for (Object value : folderList) {
                    Map o = (Map) value;
                    String name = (String) o.get("fileName");
                    fileNames.add(name);
                }
            }
            if (!fileNames.isEmpty()) {
                fileNames.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (Integer.parseInt(o2) > Integer.parseInt(o1)) {
                            return -1;
                        }
                        return 1;
                    }
                });
                for (String name : fileNames) {
                    sourceObjectList.add(ComposeSource.builder().bucket(bucketName).object(name).build());
                }
            }
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucketName1)
                            .object(fileName1)
                            .sources(sourceObjectList)
                            .build());
            return BaseBoConstants.TRUE;
        } catch (Exception e) {
            throw new BusinessException("Minio合并桶异常", e.getMessage()).setCause(e).setLog();
        }
    }

    /**
     * description: 获取桶列表
     *
     * @author bo
     * @date 2023/5/21 12:06
     */
    public static List<String> getBucketList() {
        List<Bucket> buckets = null;
        try {
            buckets = minioClient.listBuckets();
        } catch (Exception e) {
            throw new BusinessException("Minio获取桶列表失败：", e.getMessage()).setCause(e).setLog();
        }
        List<String> list = new ArrayList<>();
        for (Bucket bucket : buckets) {
            String name = bucket.name();
            list.add(name);
        }
        return list;
    }

    /**
     * description: 创建桶
     *
     * @param bucketName 桶名称
     * @author bo
     * @date 2023/5/21 12:08
     */
    public static boolean createBucket(String bucketName) {
        try {
            boolean b = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!b) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            return BaseBoConstants.TRUE;
        } catch (Exception e) {
            throw new BusinessException("Minio创建桶失败：", e.getMessage()).setCause(e).setLog();
        }
    }

    /**
     * description: 判断桶是否存在
     *
     * @author bo
     * @date 2023/5/21 12:11
     */
    public static boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new BusinessException("Minio判断桶是否存在出错：", e.getMessage()).setCause(e).setLog();
        }
    }
}
```