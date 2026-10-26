package com.cloud.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.cms.domain.CmsProductCategory;
import com.cloud.cms.mapper.CmsProductCategoryMapper;
import com.cloud.cms.service.ICmsProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product Category Service Implementation
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Service
public class CmsProductCategoryServiceImpl extends ServiceImpl<CmsProductCategoryMapper, CmsProductCategory> implements ICmsProductCategoryService {

    /**
     * Build tree structure by parentId
     */
    public List<CmsProductCategory> buildTree(List<CmsProductCategory> categories) {
        Map<String, CmsProductCategory> categoryMap = new HashMap<>();
        for (CmsProductCategory category : categories) {
            categoryMap.put(category.getId(), category);
        }
        List<CmsProductCategory> tree = new ArrayList<>();
        for (CmsProductCategory category : categories) {
            String parentId = category.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                tree.add(category);
            } else {
                CmsProductCategory parent = categoryMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }
        return tree;
    }
    /**
     * Get all leaf nodes from tree structure
     */
    public List<CmsProductCategory> getLeafNodes(List<CmsProductCategory> tree) {
        List<CmsProductCategory> leafNodes = new ArrayList<>();
        findLeafNodes(tree, leafNodes);
        return leafNodes;
    }
    /**
     * Recursively find leaf nodes
     */
    public void findLeafNodes(List<CmsProductCategory> categories, List<CmsProductCategory> leafNodes) {
        for (CmsProductCategory category : categories) {
            if (category.getChildren() == null || category.getChildren().isEmpty()) {
                leafNodes.add(category);
            } else {
                findLeafNodes(category.getChildren(), leafNodes);
            }
        }
    }
    /**
     * Main method: get all leaf nodes
     */
    public List<CmsProductCategory> getAllLeafNodes(List<CmsProductCategory> categories) {
        List<CmsProductCategory> tree = buildTree(categories);
        return getLeafNodes(tree);
    }

}
