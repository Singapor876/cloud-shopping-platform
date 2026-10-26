import request from '@/utils/request'

// 获取推荐权重配置
export function getRecommendWeight() {
  return request({
    url: '/cms/recommend/weight',
    method: 'get'
  })
}

// 修改推荐权重配置
export function updateRecommendWeight(data) {
  return request({
    url: '/cms/recommend/weight',
    method: 'put',
    data: data
  })
}