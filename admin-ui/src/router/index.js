import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: Route Configuration
 *
 * hidden: true                     // When set to true, the route will not appear in the sidebar (e.g., 401, login pages, or edit pages like /edit/1)
 * alwaysShow: true                 // When a route has more than 1 child route, it will automatically become nested mode (like component pages)
 *                                  // When there's only one child, it will display that child as the root route in the sidebar (like guide pages)
 *                                  // If you want to display the root route regardless of the number of children
 *                                  // You can set alwaysShow: true, which will ignore the previous rules and always show the root route
 * redirect: noRedirect             // When set to noRedirect, the route cannot be clicked in breadcrumb navigation
 * name:'router-name'               // Set the route name, must be filled otherwise <keep-alive> will have issues
 * query: '{"id": 1, "name": "ry"}' // Default parameters for accessing the route
 * roles: ['admin', 'common']       // Role permissions for accessing the route
 * permissions: ['a:a:a', 'b:b:b']  // Menu permissions for accessing the route
 * meta : {
    noCache: true                   // If set to true, it will not be cached by <keep-alive> (default false)
    title: 'title'                  // Set the name displayed in the sidebar and breadcrumbs
    icon: 'svg-name'                // Set the route icon, corresponding to path src/assets/icons/svg
    breadcrumb: false               // If set to false, it will not be displayed in breadcrumb
    activeMenu: '/system/user'      // When the route has this attribute, it will highlight the corresponding sidebar.
  }
 */

// Common routes
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: 'Home', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: 'Profile', icon: 'user' }
      }
    ]
  }
]

// Dynamic routes, loaded based on user permissions
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: 'Assign Role', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: 'Assign User', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: 'Dict Data', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: 'Job Log', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: 'Edit Gen Config', activeMenu: '/tool/gen' }
      }
    ]
  }
]

// Prevent errors from multiple consecutive route clicks
let routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // Remove # from URL
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
