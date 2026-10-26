import request from '@/utils/request'

// 查询购物车列表
export function listCart(query) {
  return request({
    url: '/ums/front/cart/list',
    method: 'get',
    params: query
  })
}

// 新增购物车
export function addProductInCart(data) {
  return request({
    url: '/ums/front/cart/addProductInCart',
    method: 'post',
    data: data
  })
}

// 修改购物车
export function removeProductInCart(data) {
  return request({
    url: '/ums/front/cart/removeProductInCart',
    method: 'post',
    data: data
  })
}
