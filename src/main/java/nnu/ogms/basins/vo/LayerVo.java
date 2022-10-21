package nnu.ogms.basins.vo;

import lombok.Data;

@Data
public class LayerVo {
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 风格名
     */
    private String styleName;
    /**
     * 数据存储名称
     */
    private String dataStoreName;
    /**
     * 图层名称
     */
    private String layerName;
    /**
     * 空间参考名称，默认EPSG:4326
     */
    private String crs;
    /**
     * 数据类型:1 矢量 2 tiff
     */
    private Integer dataType;
}
