package nnu.ogms.basins.vo;

import lombok.Data;

import java.util.List;

@Data
public class PublishInfoListVo {


    /**
     * 工作空间
     */
    private String workSpace;

    /**
     * 图层组
     */
    private String layerGroupName;

    /**
     * 图层组Vo
     */
    private List<LayerVo> layerGroupVoList;



}
