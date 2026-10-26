package com.cloud.system.service.impl;

import com.cloud.system.api.domain.SysLogininfor;
import com.cloud.system.mapper.SysLogininforMapper;
import com.cloud.system.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * System Access Log Information Service Layer Processing
 * 
 * @author cloud
 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService
{

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * Add system login log
     * 
     * @param logininfor Access log object
     */
    @Override
    public int insertLogininfor(SysLogininfor logininfor)
    {
        return logininforMapper.insertLogininfor(logininfor);
    }

    /**
     * Query system login log collection
     * 
     * @param logininfor Access log object
     * @return Login record collection
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)
    {
        return logininforMapper.selectLogininforList(logininfor);
    }

    /**
     * Batch delete system login logs
     * 
     * @param infoIds Login log IDs to delete
     * @return Result
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    /**
     * Clear system login logs
     */
    @Override
    public void cleanLogininfor()
    {
        logininforMapper.cleanLogininfor();
    }
}
