package com.cloud.oms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.cloud.oms.mapper.OmsOrderItemMapper;
import com.cloud.oms.domain.OmsOrderItem;
import com.cloud.oms.service.IOmsOrderItemService;

/**
 * Order Item Service Implementation
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Service
public class OmsOrderItemServiceImpl extends ServiceImpl<OmsOrderItemMapper, OmsOrderItem> implements IOmsOrderItemService {

}
