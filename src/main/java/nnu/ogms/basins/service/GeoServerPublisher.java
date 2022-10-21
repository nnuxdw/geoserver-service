package nnu.ogms.basins.service;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStoreList;
import it.geosolutions.geoserver.rest.decoder.RESTDataStoreList;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSLayerGroupEncoder;
//import it.geosolutions.geoserver.rest.encoder.datastore.GSGeoTIFFDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.GSLayerGroupEncoder23;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
import it.geosolutions.geoserver.rest.encoder.datastore.GSShapefileDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import lombok.extern.slf4j.Slf4j;
import nnu.ogms.basins.common.ErrorEnum;
import nnu.ogms.basins.common.GeneralException;
import nnu.ogms.basins.vo.LayerVo;
import nnu.ogms.basins.vo.PublishInfoListVo;
import nnu.ogms.basins.vo.PublishInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static nnu.ogms.basins.common.ErrorEnum.DATASTORE_EXIST_ERROR;

@Service
@Slf4j
public class GeoServerPublisher {

    private static String DATA_DIR;
    private static String STYLE_DIR;
    private static Resource resource;

    // geoServer信息
    private static final String GEOSERVER_URL = "http://localhost:8090/geoserver";
    private static final String GEOSERVER_USER = "admin";
    private static final String GEOSERVER_PASSWORD = "geoserver";

    static {
        try {
            DATA_DIR = ResourceUtils.getURL("classpath:").getPath() + "\\data\\";
            STYLE_DIR = ResourceUtils.getURL("classpath:").getPath() + "\\style\\";

        } catch (FileNotFoundException e) {
            log.error("获取用户目录失败:{}",e);
        }
    }


    public void publish(PublishInfoVo publishInfoVo) {
        File file = new File(DATA_DIR + publishInfoVo.getFileName());
        if (!file.exists())
        {
            throw new GeneralException(ErrorEnum.FILE_NOT_EXIST_ERROR,file.getName());
        }
        GeoServerRESTManager geoServerRESTManager = initGeoserver(publishInfoVo.getWorkSpace());
        // style样式 todo style这里可以分为：没传、自定义文件或使用已发布的
        String styleName = publishInfoVo.getStyleName();
        // 如果未指定style 或 未发布指定的style，则使用默认style
        if (StringUtils.isEmpty(styleName) || !geoServerRESTManager.getReader().existsStyle(publishInfoVo.getWorkSpace(), styleName)) {
            // 如果没有传style，就选择默认style
            String styleFilePath = STYLE_DIR + "mt_city_cite.sld";
            File styleFile = new File(styleFilePath);
            geoServerRESTManager.getPublisher().publishStyleInWorkspace(publishInfoVo.getWorkSpace(), styleFile, styleName);
        }else {

        }
        //
        if (publishInfoVo.getDataType() == 1){
            // 发布矢量图层
            publishShape(geoServerRESTManager,publishInfoVo,file);
        }else if (publishInfoVo.getDataType() == 2){
            // 发布栅格图层
            publishTiff(geoServerRESTManager,publishInfoVo,file);
        }
    }

    private GeoServerRESTManager initGeoserver(String workspace){



        // 连接geoServer
        GeoServerRESTManager geoServerRESTManager = null;
        try {
            geoServerRESTManager = new GeoServerRESTManager(new URL(GEOSERVER_URL), GEOSERVER_USER, GEOSERVER_PASSWORD);
        } catch (Exception e) {
            log.error("failed to connect GeoServer,reason is:{}",e);
        }

        // shp读写和发布
        assert geoServerRESTManager != null;
        GeoServerRESTReader restReader = geoServerRESTManager.getReader();
        GeoServerRESTPublisher restPublisher = geoServerRESTManager.getPublisher();

        // 存在相应的工作区
        if (!restReader.existsWorkspace(workspace)) {
            log.info("create workspace_name: {}" + workspace);
            restPublisher.createWorkspace(workspace);
        }else {
            log.info("workspace {} already exists.", workspace);
        }
        return geoServerRESTManager;
    }


    /**
     * 在指定工作空间下创建图层组,并发布图层服务
     *
     * @return 图层组是否创建成功
     */
    public void publishLayerGroup(PublishInfoListVo publishInfoListVo) {

        GeoServerRESTManager geoServerRESTManager = initGeoserver(publishInfoListVo.getWorkSpace());
        // 已存在图层组则不创建
        if (geoServerRESTManager.getReader().existsLayerGroup(publishInfoListVo.getWorkSpace(), publishInfoListVo.getLayerGroupName())) {
            log.warn("layer group {} already exists",publishInfoListVo.getLayerGroupName());
//            throw new GeneralException(ErrorEnum.LAYER_GROUP_EXIST_ERROR);
        }else {
            GSLayerGroupEncoder24 gsLayerGroupEncoder = new GSLayerGroupEncoder24();
//            gsLayerGroupEncoder.setWorkspace(publishInfoListVo.getWorkSpace());
//            gsLayerGroupEncoder.setName(publishInfoListVo.getLayerGroupName());
//          // 默认给图层组一个空白的灰白图层，不然图层组创建不成功  这个图层应该是默认就有的
//            gsLayerGroupEncoder.addLayer("tiger:giant_polygon");
            for (LayerVo layerVo : publishInfoListVo.getLayerGroupVoList()) {

                String layerWorkspaceName = publishInfoListVo.getWorkSpace();
                String layerName = layerVo.getLayerName();
                // 图层不存在则创建
                if (!geoServerRESTManager.getReader().existsLayer(layerWorkspaceName, layerName)) {
                    log.info("layer {} not exist,creating...",layerName);
                    File file = new File(DATA_DIR + layerVo.getFileName());
                    if (!file.exists())
                    {
                        throw new GeneralException(ErrorEnum.FILE_NOT_EXIST_ERROR,file.getName());
                    }
                    // 发布矢量或栅格图层
                    if (layerVo.getDataType() == 1){
                        this.publishShape(geoServerRESTManager,new PublishInfoVo(publishInfoListVo.getWorkSpace(),layerVo),file);
                    }else if (layerVo.getDataType() == 2){
                        this.publishTiff(geoServerRESTManager,new PublishInfoVo(publishInfoListVo.getWorkSpace(),layerVo),file);
                    }
                    String layerFullName = layerWorkspaceName + ":" + layerName;
                    gsLayerGroupEncoder.addLayer(layerFullName,layerVo.getStyleName());
                }else {
                    RESTLayer layer = geoServerRESTManager.getReader().getLayer(layerWorkspaceName,layerVo.getLayerName());
                    gsLayerGroupEncoder.addLayer(layer.getName(),layerVo.getStyleName());
                }
            }
            gsLayerGroupEncoder.setTitle(publishInfoListVo.getLayerGroupName());
            // 组成图层组
            boolean layerGroup = createLayerGroup(publishInfoListVo.getWorkSpace(), publishInfoListVo.getLayerGroupName(), gsLayerGroupEncoder);

            log.info("layer group {} create successfully",layerGroup);
        }

    }


    private void publishShape(GeoServerRESTManager geoServerRESTManager, PublishInfoVo publishInfoVo, File file){
        // todo 1.layer名称是否要和shp名称一致？ 2. style问题  3.一个图层对应一个数据源，批量发布需要使用图层组 4.投影问题
        // 存在相应的layer
        if (geoServerRESTManager.getReader().existsLayer(publishInfoVo.getWorkSpace(),publishInfoVo.getLayerName(),true)){
            log.error("layer {} already exists", publishInfoVo.getLayerName());
            throw new GeneralException(ErrorEnum.LAYER_EXIST_ERROR);
        }
        // 存在相应的数据存储
        RESTDataStoreList datastoresList = geoServerRESTManager.getReader().getDatastores(publishInfoVo.getWorkSpace());
        List<String> datastoreNameList = datastoresList.getNames();
        if (!CollectionUtils.isEmpty(datastoreNameList) && datastoreNameList.contains(publishInfoVo.getDataStoreName())){
            log.error("data store {} already exists", publishInfoVo.getDataStoreName());
            throw new GeneralException(DATASTORE_EXIST_ERROR);
        }
        // 校验shp文件名的后缀必须为zip
        String[] splitList = publishInfoVo.getFileName().split("\\.");
        //    如果后缀名不是 zip 则直接报错
        if (!StringUtils.equals(splitList[1], "zip")) {
            throw new GeneralException(ErrorEnum.SHP_FILE_NOT_ZIP_ERROR);
        }
        // 另一种方式判断 矢量数据存储是否存在
//        if (geoServerRESTManager.getReader().existsDatastore(publishInfoVo.getWorkSpace(),publishInfoVo.getDataStoreName())){
//            log.error("data store {} already exists", publishInfoVo.getDataStoreName());
//            throw new GeneralException(DATASTORE_EXIST_ERROR);
//        }
        //创建shape文件存储
        try {
            //shp文件所在的位置
            String urlDataStorePath = file.getPath();

            // 数据存储需要的文件
            String shpFilePath = String.format("file://%s", urlDataStorePath);
            URL urlShapeFile = new URL(shpFilePath);
            // 创建矢量数据集
            GSShapefileDatastoreEncoder datastoreEncoder = new GSShapefileDatastoreEncoder(publishInfoVo.getDataStoreName(), urlShapeFile);
            datastoreEncoder.setCharset(Charset.forName("GBK"));
            geoServerRESTManager.getStoreManager().create(publishInfoVo.getWorkSpace(), datastoreEncoder);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // 发布服务
        try {
            GSFeatureTypeEncoder gsFeatureTypeEncoder = new GSFeatureTypeEncoder();
            gsFeatureTypeEncoder.setTitle(publishInfoVo.getLayerName());
            gsFeatureTypeEncoder.setName(publishInfoVo.getLayerName());
            gsFeatureTypeEncoder.setSRS(GeoServerRESTPublisher.DEFAULT_CRS);

            GSLayerEncoder gsLayerEncoder = new GSLayerEncoder();
            gsLayerEncoder.addStyle(publishInfoVo.getStyleName());
            boolean layer = geoServerRESTManager.getPublisher().publishShp(publishInfoVo.getWorkSpace(), publishInfoVo.getDataStoreName(),publishInfoVo.getLayerName(), file,publishInfoVo.getCrs(),publishInfoVo.getStyleName());

//            boolean layer = geoServerRESTManager.getPublisher().publishDBLayer(publishInfoVo.getWorkSpace(), publishInfoVo.getDataStoreName(), gsFeatureTypeEncoder, gsLayerEncoder);
            log.info("publish layer:{}",layer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void publishTiff(GeoServerRESTManager geoServerRESTManager, PublishInfoVo publishInfoVo, File file) {
        // 存在相应的layer
        if (geoServerRESTManager.getReader().existsLayer(publishInfoVo.getWorkSpace(),publishInfoVo.getLayerName(),true)){
            log.error("layer {} already exists", publishInfoVo.getLayerName());
            throw new GeneralException(ErrorEnum.LAYER_EXIST_ERROR);
        }
        // 栅格数据源是否存在
        RESTCoverageStoreList coverageStoreList = geoServerRESTManager.getReader().getCoverageStores(publishInfoVo.getWorkSpace());
        List<String> coverageNameList = coverageStoreList.getNames();
        if (!CollectionUtils.isEmpty(coverageNameList) && coverageNameList.contains(publishInfoVo.getDataStoreName())){
            log.error("data store {} already exists", publishInfoVo.getDataStoreName());
            throw new GeneralException(DATASTORE_EXIST_ERROR);
        }
//        else {
//            //创建文件存储
//            try {
//                String urlDataStorePath = file.getPath();
//                // 数据存储需要的文件
//                String shpFilePath = String.format("file://%s", urlDataStorePath);
//                URL urlFile = new URL(shpFilePath);
//                GSGeoTIFFDatastoreEncoder gsGeoTIFFDatastoreEncoder = new GSGeoTIFFDatastoreEncoder(publishInfoVo.getDataStoreName());
//                gsGeoTIFFDatastoreEncoder.setWorkspaceName(publishInfoVo.getWorkSpace());
//                gsGeoTIFFDatastoreEncoder.setUrl(urlFile);
//                boolean createStore = geoServerRESTManager.getStoreManager().create(publishInfoVo.getWorkSpace(), gsGeoTIFFDatastoreEncoder);
//                log.info("create store status: {}" , createStore);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        // 另一种方式判断 栅格数据存储是否存在
//        if (geoServerRESTManager.getReader().existsCoveragestore(publishInfoVo.getWorkSpace(), publishInfoVo.getDataStoreName())){
//            log.error("data store {} already exists", publishInfoVo.getDataStoreName());
//            throw new GeneralException(DATASTORE_EXIST_ERROR);
//        }

        // 发布服务
        try {
            boolean publish = geoServerRESTManager.getPublisher().publishGeoTIFF(publishInfoVo.getWorkSpace(), publishInfoVo.getDataStoreName(),publishInfoVo.getLayerName(), file,publishInfoVo.getCrs(), GSResourceEncoder.ProjectionPolicy.NONE,publishInfoVo.getStyleName());
            log.info("tiff publish status: {}" , publish);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


//    private void publishRaster(GeoServerRESTManager geoServerRESTManager,PublishInfoVo publishInfoVo,File file){
//        //shp文件所在的位置
//        String urlDataStorePath = file.getPath();
//        // 数据存储需要的文件
//        String filePath = String.format("file://%s", urlDataStorePath);
//        // 创建栅格数据集
////        GSCoverageEncoder gsCoverageEncoder = new GSCoverageEncoder();
////        gsCoverageEncoder.setName(publishInfoVo.getLayerName());
////        gsCoverageEncoder.setTitle(publishInfoVo.getLayerName());
////        gsCoverageEncoder.setSRS("EPSG");
////        gsCoverageEncoder.setNativeFormat("GeoTIFF");
////        gsCoverageEncoder.addSupportedFormats("GEOTIFF");
////        gsCoverageEncoder.setNativeCRS("EPSG");
////        gsCoverageEncoder.setRequestSRS("EPSG");
////        gsCoverageEncoder.setResponseSRS("EPSG");
////        gsCoverageEncoder.addKeyword("WCS");
////        gsCoverageEncoder.setProjectionPolicy(GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED);
////        gsCoverageEncoder.setLatLonBoundingBox(-180, -90, 180, 90, GeoServerRESTPublisher.DEFAULT_CRS);
////        gsCoverageEncoder.addKeyword("geoTiff");
////        gsCoverageEncoder.addKeyword("WCS");
//        if (geoServerRESTManager.getReader().getDatastore(publishInfoVo.getWorkSpace(),publishInfoVo.getDataStoreName()) == null) {
//            GSGeoTIFFDatastoreEncoder gsGeoTIFFDatastoreEncoder = new GSGeoTIFFDatastoreEncoder(publishInfoVo.getDataStoreName());
//            gsGeoTIFFDatastoreEncoder.setWorkspaceName(publishInfoVo.getWorkSpace());
//            gsGeoTIFFDatastoreEncoder.setUrl(new URL(filePath));
//            boolean createStore = geoServerRESTManager.getStoreManager().create(workspace_name, gsGeoTIFFDatastoreEncoder);
//            System.out.println("create store (TIFF文件创建状态) : " + createStore);
//
//            boolean publish = geoServerRESTManager.getPublisher().publishGeoTIFF(workspace_name, store_name, new File(file_name));
//            System.out.println("publish (TIFF文件发布状态) : " + publish);
//
//        } else {
//            System.out.println("数据存储已经存在了,store:" + store_name);
//        }
//        geoServerRESTManager.getStoreManager().create(publishInfoVo.getWorkSpace(), gsCoverageEncoder);
//        boolean createStore = geoServerRESTManager.getPublisher().createCoverage(publishInfoVo.getWorkSpace(),publishInfoVo.getDataStoreName(),gsCoverageEncoder);
//        System.out.println("Coverage store " + createStore);
//
//        boolean publish = false;
//        try {
//            publish = geoServerRESTManager.getPublisher().publishGeoTIFF(publishInfoVo.getWorkSpace(), publishInfoVo.getLayerName(),file);
//        } catch (FileNotFoundException e) {
//            throw new GeneralException(ErrorEnum.FILE_NOT_EXIST_ERROR,file.getName());
//        }
//        System.out.println("publish Coverage " + publish);
//
//    }



    public boolean createLayerGroup(String workspace, String name, MyGSLayerGroupEncoder group) {

        String url = "";
        try {
            url = HTTPUtils.decurtSlash(new URL(GEOSERVER_URL).toString()) + "/rest";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (workspace == null) {
            url += "/layergroups/";
        } else {
            group.setWorkspace(workspace);
            url += "/workspaces/" + workspace + "/layergroups/";
        }

        group.setName(name);
        String groupStr = group.toString();
        log.info("Layer group xml is: {}",groupStr);
        String sendResult = HTTPUtils.postXml(url, groupStr, GEOSERVER_USER, GEOSERVER_PASSWORD);
        if (sendResult != null) {
            if (log.isInfoEnabled()) {
                log.info("LayerGroup {} successfully configured " , name);
            }
        } else {
            if (log.isWarnEnabled())
                log.warn("Error configuring LayerGroup {}, result is {}  ",name,sendResult);
        }

        return sendResult != null;
    }


    public void publishLayersTest(){

        GSLayerGroupEncoder24 groupWriter = new GSLayerGroupEncoder24();
        groupWriter.addLayer("topp:states","cite_lakes");
        groupWriter.setTitle("tasmania2");
        createLayerGroup("topp","tasmania2",groupWriter);
    }
}
