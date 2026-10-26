import request from '@/utils/request'

// 获取后台首页统计数据
export function getDashboardSummary() {
  return request({
    url: '/cms/dashboard/summary',
    method: 'get'
  })
}