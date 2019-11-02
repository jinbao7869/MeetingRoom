/** 
 * DeviceDTO.Java
 * <p>
 * Copyright(C) 2017-2019 Hangzhou Zhuo Jian Mdt InfoTech Ltd
 * </p>
 * All rights reserved
 * ------------------------------------------
 * @Date: 2019-11-02 16:56:10 Created
 * @Author: Technical team of walking
 * @ProjectName: meeting
 */
package com.walking.meeting.dataobject.dto;

import cn.ucmed.yilian.common.utils.annotation.IdEncryption;
import java.util.Date;
import lombok.Data;

@Data
public class DeviceDTO {
    /**
     * 设备表id
     */
    @IdEncryption
    private String id;

    /**
     * 设备类型名称
     */
    private String deviceType;

    /**
     * 设备id,1投影仪,2电插头,3遥控器,4黑板,5HDMI接口
     */
    private Integer deviceId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后一次更新时间
     */
    private Date updateTime;

    /**
     * 有值则已经被删除
     */
    private Date deleteTime;
}