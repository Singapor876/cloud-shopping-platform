package com.cloud.system.service.impl;

import com.cloud.system.domain.SysNotice;
import com.cloud.system.mapper.SysNoticeMapper;
import com.cloud.system.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notice Service Implementation
 * 
 * @author cloud
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService
{
    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     * Query notice information
     * 
     * @param noticeId Notice ID
     * @return Notice information
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId)
    {
        return noticeMapper.selectNoticeById(noticeId);
    }

    /**
     * Query notice list
     * 
     * @param notice Notice information
     * @return Notice collection
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice)
    {
        return noticeMapper.selectNoticeList(notice);
    }

    /**
     * Add notice
     * 
     * @param notice Notice information
     * @return Result
     */
    @Override
    public int insertNotice(SysNotice notice)
    {
        return noticeMapper.insertNotice(notice);
    }

    /**
     * Update notice
     * 
     * @param notice Notice information
     * @return Result
     */
    @Override
    public int updateNotice(SysNotice notice)
    {
        return noticeMapper.updateNotice(notice);
    }

    /**
     * Delete notice object
     * 
     * @param noticeId Notice ID
     * @return Result
     */
    @Override
    public int deleteNoticeById(Long noticeId)
    {
        return noticeMapper.deleteNoticeById(noticeId);
    }

    /**
     * Batch delete notice information
     * 
     * @param noticeIds Notice IDs to delete
     * @return Result
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds)
    {
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }
}
