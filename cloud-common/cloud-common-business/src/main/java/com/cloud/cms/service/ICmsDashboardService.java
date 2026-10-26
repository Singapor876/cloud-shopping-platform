package com.cloud.cms.service;

import com.cloud.cms.domain.vo.CmsDashboardSummary;

/**
 * 首页统计面板Service接口
 */
public interface ICmsDashboardService {

    CmsDashboardSummary getSummary();
}