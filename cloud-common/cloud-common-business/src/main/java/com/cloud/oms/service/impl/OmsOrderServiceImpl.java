package com.cloud.oms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.cloud.oms.mapper.OmsOrderMapper;
import com.cloud.oms.domain.OmsOrder;
import com.cloud.oms.service.IOmsOrderService;

/**
 * Order Service Implementation
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements IOmsOrderService {

}
