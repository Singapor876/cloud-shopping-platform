package com.cloud.system.service.impl;

import com.cloud.common.core.constant.Constants;
import com.cloud.common.core.constant.UserConstants;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.text.Convert;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.redis.service.RedisService;
import com.cloud.system.domain.SysConfig;
import com.cloud.system.mapper.SysConfigMapper;
import com.cloud.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * Parameter Configuration Service Implementation
 * 
 * @author cloud
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisService redisService;

    /**
     * Initialize parameters to cache on project startup
     */
    @PostConstruct
    public void init()
    {
        loadingConfigCache();
    }

    /**
     * Query parameter configuration information
     * 
     * @param configId Parameter configuration ID
     * @return Parameter configuration information
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * Query parameter configuration information by key name
     * 
     * @param configKey Parameter key
     * @return Parameter value
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
        String configValue = Convert.toStr(redisService.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue))
        {
            return configValue;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisService.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Query parameter configuration list
     * 
     * @param config Parameter configuration information
     * @return Parameter configuration collection
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * Add parameter configuration
     * 
     * @param config Parameter configuration information
     * @return Result
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * Update parameter configuration
     * 
     * @param config Parameter configuration information
     * @return Result
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * Batch delete parameter information
     * 
     * @param configIds Parameter IDs to delete
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("Built-in parameter [%1$s] cannot be deleted ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            redisService.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * Load parameter cache data
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * Clear parameter cache data
     */
    @Override
    public void clearConfigCache()
    {
        Collection<String> keys = redisService.keys(Constants.SYS_CONFIG_KEY + "*");
        redisService.deleteObject(keys);
    }

    /**
     * Reset parameter cache data
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * Check if parameter key name is unique
     * 
     * @param config Parameter configuration information
     * @return Result
     */
    @Override
    public String checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Set cache key
     * 
     * @param configKey Parameter key
     * @return Cache key
     */
    private String getCacheKey(String configKey)
    {
        return Constants.SYS_CONFIG_KEY + configKey;
    }
}
