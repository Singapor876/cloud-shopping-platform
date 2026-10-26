import Home from "@/views/Home.vue";
import PromotionDetail from "@/views/promotion/PromotionDetail.vue";
import Product from "@/views/index/Product.vue";
import Login from "@/views/Login.vue";
import PersonalCenter from "@/views/personal/PersonalCenter.vue";
import MyOrders from "@/views/personal/MyOrders.vue";
import MyCart from "@/views/personal/MyCart.vue";
import Favorites from "@/views/personal/Favorites.vue";
import MyReviews from "@/views/personal/MyReviews.vue";
import BrowseHistory from "@/views/personal/BrowseHistory.vue";
import ProductDetail from "@/views/index/ProductDetail.vue";
import VueRouter from "vue-router";
import OrderDetail from "@/views/order/OrderDetail.vue";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        meta: {
            title: 'Home'
        },
    },
    {
        path: '/promotion/:id',
        name: 'PromotionDetail',
        component: PromotionDetail,
        meta: {
            title: 'Promotion Details'
        },
    },
    {
        path: '/product',
        name: 'Product',
        component: Product,
        meta: {
            title: 'Product List'
        },
    },
    {
        path: '/login',
        name: '/login',
        component: Login,
        meta: {
            title: 'Login'
        },
    },
    {
        path: "/product/:id",
        name: "ProductDetail",
        component: ProductDetail,
        meta: {
            title: 'Product Details'
        },
    },
    {
        path: "/personal-center",
        name: "personal-center",
        component: PersonalCenter,
        meta: {
            title: 'Personal Center'
        },
    },{
        path: "/order-detail/:orderId",
        name: "order-detail",
        component: OrderDetail,
        meta: {
            title: 'Order Details'
        },
    },
    {
        path: "/my-orders",
        name: "my-orders",
        component: MyOrders,
        meta: {
            title: 'My Orders'
        },
    },
    {
        path: "/my-cart",
        name: "my-cart",
        component: MyCart,
        meta: {
            title: 'My Shopping Cart'
        },
    },
    {
        path: "/favorites",
        name: "favorites",
        component: Favorites,
        meta: {
            title: 'My Favorites'
        },
    },
    {
        path: "/my-reviews",
        name: "my-reviews",
        component: MyReviews,
        meta: {
            title: 'My Reviews'
        },
    },
    {
        path: "/browse-history",
        name: "browse-history",
        component: BrowseHistory,
        meta: {
            title: 'Browse History'
        },
    },
];

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes,
});

export default router;
