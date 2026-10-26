package com.cloud.system.service.impl;

import com.cloud.common.core.constant.UserConstants;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.system.domain.SysPost;
import com.cloud.system.mapper.SysPostMapper;
import com.cloud.system.mapper.SysUserPostMapper;
import com.cloud.system.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Post Information Service Layer Processing
 * 
 * @author cloud
 */
@Service
public class SysPostServiceImpl implements ISysPostService
{
    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    /**
     * Query post information collection
     * 
     * @param post Post information
     * @return Post information collection
     */
    @Override
    public List<SysPost> selectPostList(SysPost post)
    {
        return postMapper.selectPostList(post);
    }

    /**
     * Query all posts
     * 
     * @return Post list
     */
    @Override
    public List<SysPost> selectPostAll()
    {
        return postMapper.selectPostAll();
    }

    /**
     * Query post information by post ID
     * 
     * @param postId Post ID
     * @return Role object information
     */
    @Override
    public SysPost selectPostById(Long postId)
    {
        return postMapper.selectPostById(postId);
    }

    /**
     * Get post selection list by user ID
     * 
     * @param userId User ID
     * @return Selected post ID list
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId)
    {
        return postMapper.selectPostListByUserId(userId);
    }

    /**
     * Check if post name is unique
     * 
     * @param post Post information
     * @return Result
     */
    @Override
    public String checkPostNameUnique(SysPost post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostNameUnique(post.getPostName());
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if post code is unique
     * 
     * @param post Post information
     * @return Result
     */
    @Override
    public String checkPostCodeUnique(SysPost post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostCodeUnique(post.getPostCode());
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Query post usage count by post ID
     * 
     * @param postId Post ID
     * @return Result
     */
    @Override
    public int countUserPostById(Long postId)
    {
        return userPostMapper.countUserPostById(postId);
    }

    /**
     * Delete post information
     * 
     * @param postId Post ID
     * @return Result
     */
    @Override
    public int deletePostById(Long postId)
    {
        return postMapper.deletePostById(postId);
    }

    /**
     * Batch delete post information
     * 
     * @param postIds Post IDs to delete
     * @return Result
     */
    @Override
    public int deletePostByIds(Long[] postIds)
    {
        for (Long postId : postIds)
        {
            SysPost post = selectPostById(postId);
            if (countUserPostById(postId) > 0)
            {
                throw new ServiceException(String.format("%1$s is already assigned, cannot be deleted", post.getPostName()));
            }
        }
        return postMapper.deletePostByIds(postIds);
    }

    /**
     * Add and save post information
     * 
     * @param post Post information
     * @return Result
     */
    @Override
    public int insertPost(SysPost post)
    {
        return postMapper.insertPost(post);
    }

    /**
     * Update and save post information
     * 
     * @param post Post information
     * @return Result
     */
    @Override
    public int updatePost(SysPost post)
    {
        return postMapper.updatePost(post);
    }
}
