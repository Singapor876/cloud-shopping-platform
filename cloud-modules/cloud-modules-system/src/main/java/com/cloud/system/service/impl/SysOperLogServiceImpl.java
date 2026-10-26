package com.cloud.system.service.impl;

import com.cloud.system.api.domain.SysOperLog;
import com.cloud.system.mapper.SysOperLogMapper;
import com.cloud.system.service.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Operation Log Service Layer Processing
 * 
 * @author cloud
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService
{
    @Autowired
    private SysOperLogMapper operLogMapper;

    /**
     * Add operation log
     * 
     * @param operLog Operation log object
     * @return Result
     */
    @Override
    public int insertOperlog(SysOperLog operLog)
    {
        return operLogMapper.insertOperlog(operLog);
    }

    /**
     * Query system operation log collection
     * 
     * @param operLog Operation log object
     * @return Operation log collection
     */
    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog)
    {
        return operLogMapper.selectOperLogList(operLog);
    }

    /**
     * Batch delete system operation logs
     * 
     * @param operIds Operation log IDs to delete
     * @return Result
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds)
    {
        return operLogMapper.deleteOperLogByIds(operIds);
    }

    /**
     * Query operation log details
     * 
     * @param operId Operation ID
     * @return Operation log object
     */
    @Override
    public SysOperLog selectOperLogById(Long operId)
    {
        return operLogMapper.selectOperLogById(operId);
    }

    /**
     * Clear operation logs
     */
    @Override
    public void cleanOperLog()
    {
        operLogMapper.cleanOperLog();
    }
}
