package com.zhouzifei.tool.fileClient;


import com.qcloud.cos.utils.IOUtils;
import com.zhouzifei.cache.FileCacheEngine;
import com.zhouzifei.tool.common.ServiceException;
import com.zhouzifei.tool.common.fastdfs.*;
import com.zhouzifei.tool.common.fastdfs.common.NameValuePair;
import com.zhouzifei.tool.config.FastDfsFileProperties;
import com.zhouzifei.tool.config.FileProperties;
import com.zhouzifei.tool.consts.StorageTypeConst;
import com.zhouzifei.tool.consts.UpLoadConstant;
import com.zhouzifei.tool.dto.CheckFileResult;
import com.zhouzifei.tool.dto.VirtualFile;
import com.zhouzifei.tool.entity.FileListRequesr;
import com.zhouzifei.tool.entity.MetaDataRequest;
import com.zhouzifei.tool.service.ApiClient;
import com.zhouzifei.tool.util.FileUtil;
import com.zhouzifei.tool.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.zhouzifei.tool.consts.UpLoadConstant.ZERO_INT;

/**
 * @author 周子斐
 * @date 2021/1/30
 * @Description
 */
@Component
@Slf4j
public class FastDfsOssApiClient extends BaseApiClient {

    private String serverUrl;

    public FastDfsOssApiClient() {
        super(StorageTypeConst.FASTDFS.getStorageType());
    }
    public FastDfsOssApiClient(FileProperties fileProperties) {
        super(StorageTypeConst.FASTDFS.getStorageType());
        init(fileProperties);
    }

    @Override
    public FastDfsOssApiClient init(FileProperties fileProperties) {
        final FastDfsFileProperties fastDfsFileProperties = fileProperties.getFast();
        this.serverUrl = fastDfsFileProperties.getServerUrl();
        this.domainUrl = fastDfsFileProperties.getUrl();
        checkDomainUrl(domainUrl);
        Properties props = new Properties();
        props.put(ClientGlobal.PROP_KEY_TRACKER_SERVERS, serverUrl);
        try {
            ClientGlobal.initByProperties(props);
        } catch (IOException e) {
            throw new ServiceException("[" + this.storageType + "]尚未配置FastDfs，文件上传功能暂时不可用！");
        }
        return this;
    }

    @Override
    public String uploadInputStream(InputStream is, String fileName) {
        try {
            //tracker 客户端
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerServer
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //创建StorageClient 对象
            StorageClient storageClient = new StorageClient(trackerServer);
            //文件元数据信息组
            NameValuePair[] nameValuePairs = {new NameValuePair("author", "huhy")};
            byte[] bytes = IOUtils.toByteArray(is);
            final String suffix = FileUtil.getSuffix(fileName);
            String[] txts = storageClient.upload_file(bytes, suffix, nameValuePairs);
            return String.join("/", txts);
        } catch (IOException var6) {
            log.info("上传失败,失败原因{}", var6.getMessage());
            throw new ServiceException("文件上传异常!");
        }
    }

    @Override
    public boolean removeFile(String fileName) {
        if (StringUtils.isNullOrEmpty(fileName)) {
            throw new ServiceException("[" + this.storageType + "]删除文件失败：文件key为空");
        }
        //tracker 客户端
        TrackerClient trackerClient = new TrackerClient();
        //获取trackerServer
        try {
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //创建StorageClient 对象
            StorageClient storageClient = new StorageClient(trackerServer);
            final String group = getGroup(fileName);
            final String filePath = getFilePath(fileName);
            FileInfo fileInfo = storageClient.query_file_info(group, filePath);
            if (null != fileInfo) {
                //文件元数据信息组
                final int deleteFile = storageClient.delete_file(group, filePath);
                return !(deleteFile == 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public CheckFileResult checkFile(MetaDataRequest metaDataRequest, HttpServletRequest request) {
        //storageClient.deleteFile(UpLoadConstant.DEFAULT_GROUP, "M00/00/D1/eSqQlFsM_RWASgIyAAQLLONv59s385.jpg");
        String userName = (String) request.getSession().getAttribute("name");
        FileCacheEngine fileCacheEngine = new FileCacheEngine();
        if (StringUtils.isEmpty(userName)) {
            request.getSession().setAttribute("name", "yxqy");
        }
        String fileMd5 = metaDataRequest.getFileMd5();
        if (StringUtils.isEmpty(fileMd5)) {
            throw new ServiceException("fileMd5不能为空");
        }
        CheckFileResult checkFileResult = new CheckFileResult();
        //模拟从mysql中查询文件表的md5,这里从redis里查询
        //查询锁占用
        String lockName = UpLoadConstant.currLocks + fileMd5;
        Integer i = fileCacheEngine.get(fileMd5, lockName, Integer.class);
        if (null == i) {
            i = 0;
            fileCacheEngine.add(fileMd5, lockName, "0");
        }
        int lock = i + 1;
        fileCacheEngine.add(fileMd5, lockName, lock);
        String lockOwner = UpLoadConstant.lockOwner + fileMd5;
        String chunkCurrkey = UpLoadConstant.chunkCurr + fileMd5;
        if (lock > 1) {
            checkFileResult.setLock(1);
            //检查是否为锁的拥有者,如果是放行
            String oWner = fileCacheEngine.get(fileMd5, lockOwner, String.class);
            if (StringUtils.isEmpty(oWner)) {
                throw new ServiceException("无法获取文件锁拥有者");
            } else {
                if (oWner.equals(request.getSession().getAttribute("name"))) {
                    String chunkCurr = (String) fileCacheEngine.get(fileMd5, chunkCurrkey);
                    if (StringUtils.isEmpty(chunkCurr)) {
                        throw new ServiceException("无法获取当前文件chunkCurr");
                    }
                    checkFileResult.setChunkCurr(Integer.parseInt(chunkCurr));
                    return checkFileResult;
                } else {
                    throw new ServiceException("当前文件已有人在上传,您暂无法上传该文件");
                }
            }
        } else {
            //初始化锁.分块
            fileCacheEngine.add(fileMd5, lockOwner, request.getSession().getAttribute("name"));
            fileCacheEngine.add(fileMd5, chunkCurrkey, "0");
            checkFileResult.setChunkCurr(0);
            return checkFileResult;
        }
    }

    @Override
    public List<VirtualFile> fileList(FileListRequesr fileListRequesr){
        //tracker 客户端
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getTrackerServer();//创建StorageClient 对象
            StorageClient storageClient = new StorageClient(trackerServer);
            //FileInfo fileInfo = storageClient.(group, filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean exists(String fileName) {
        return false;
    }

    @Override
    public VirtualFile multipartUpload(InputStream inputStream, MetaDataRequest metaDataRequest) {
        Date startTime = new Date();
        final String name = metaDataRequest.getName();
        final Integer chunkSize = metaDataRequest.getChunkSize();
        final Integer chunk = metaDataRequest.getChunk();
        final Integer chunks = metaDataRequest.getChunks();
        final String fileMd5 = metaDataRequest.getFileMd5();
        final Long size = metaDataRequest.getSize();
        String fileExtName = FileUtil.getSuffix(String.valueOf(name));
        try {
            //tracker 客户端
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerServer
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //创建StorageClient 对象
            StorageClient storageClient = new StorageClient(trackerServer);
            //文件元数据信息组
            NameValuePair[] nameValuePairs = {new NameValuePair("author", "huhy")};
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            if (chunk.equals(ZERO_INT)) {
                String[] strings = storageClient.upload_appender_file(UpLoadConstant.DEFAULT_GROUP, bytes, 0, chunkSize, fileExtName, nameValuePairs);
                String path = strings[1];
                super.cacheEngine.add(storageType, fileMd5, path);
            } else {
                Long offset;
                if (chunk == chunks - 1) {
                    offset = size - chunkSize;
                } else {
                    offset = (long) chunk * chunkSize;
                }
                final Object o = cacheEngine.get(storageType, fileMd5);
                final String path = String.valueOf(o);
                storageClient.modify_file(UpLoadConstant.DEFAULT_GROUP, path, offset, bytes);
            }
            if (FileUtil.addChunkAndCheckAllDone(fileMd5, chunks)) {
                final Object o = cacheEngine.get(storageType, fileMd5);
                cacheEngine.remove(storageType, fileMd5);
                final String filePath = UpLoadConstant.DEFAULT_GROUP + SLASH + o;
                return VirtualFile.builder()
                        .originalFileName(FileUtil.getName(String.valueOf(name)))
                        .suffix(this.suffix)
                        .uploadStartTime(startTime)
                        .uploadEndTime(new Date())
                        .filePath(this.newFileName)
                        .fileHash(null)
                        .fullFilePath(this.domainUrl + this.newFileName).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void check() {

    }

    public static String getGroup(String fileName) {
        final String[] split = fileName.split("/");
        return split[0];
    }

    public static String getFilePath(String fileName) {
        final String[] split = fileName.split("/");
        List<String> strings = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
        return String.join("/", strings);
    }

    @Override
    public InputStream downloadFileStream(String fileName) {
        try {
            //tracker 客户端
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerServer
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //创建StorageClient 对象
            StorageClient storageClient = new StorageClient(trackerServer);
            final String group = getGroup(fileName);
            final String filePath = getFilePath(fileName);
            FileInfo fileInfo = storageClient.query_file_info(group, filePath);
            if (null == fileInfo) {
                throw new ServiceException("文件不存在");
            }
            byte[] bytes = storageClient.download_file(group, filePath);
            return new ByteArrayInputStream(bytes);
        } catch (IOException var6) {
            log.info("上传失败,失败原因{}", var6.getMessage());
            throw new ServiceException("文件上传异常!");
        }
    }

    @Override
    public ApiClient getAwsApiClient(){
        throw new ServiceException("[" + this.storageType + "]暂不支持AWS3协议！");
    }
}
