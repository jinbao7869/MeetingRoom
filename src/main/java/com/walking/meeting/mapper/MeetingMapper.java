/** 
 * MeetingMapper.Java
 * <p>
 * Copyright(C) 2017-2019 Hangzhou Zhuo Jian Mdt InfoTech Ltd
 * </p>
 * All rights reserved
 * ------------------------------------------
 * @Date: 2019-11-02 16:56:06 Created
 * @Author: Technical team of walking
 * @ProjectName: meeting
 */
package com.walking.meeting.mapper;

import com.walking.meeting.dataobject.dao.MeetingDO;
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
public interface MeetingMapper extends Mapper<MeetingDO> {
}