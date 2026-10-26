import request from '@/utils/request'

// 获取推荐商品列表
export function getRecommendProducts() {
  return request({
    url: '/cms/front/recommend/products',
    method: 'get'
  })
}