import axios from 'axios';
import { Notification, MessageBox, Message } from 'element-ui';
import { getToken } from '@/utils/auth';
import errorCode from '@/utils/errorCode';
import cache from '@/plugins/cache';
import { tansParams } from '@/utils/customize';
import store from '@/store'; // Import Vuex store
import router from '@/router'; // Import router instance

// Whether to show relogin
export let isRelogin = { show: false };

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8';

// Create axios instance
const service = axios.create({
    // axios request configuration has baseURL option, representing common part of request URL
    baseURL: process.env.VUE_APP_BASE_API,
    // Timeout
    timeout: 10000,
});

// Request interceptor
service.interceptors.request.use(
    (config) => {
        console.log('Request Interceptor - Setting loading to true'); // Debug info
        store.dispatch('loading/setLoading', true); // Show loading status
        // Whether to set token
        const isToken = (config.headers || {}).isToken === false;
        // Whether to prevent duplicate submission
        const isRepeatSubmit = (config.headers || {}).repeatSubmit === false;
        if (getToken() && !isToken) {
            config.headers['frontAuth'] = getToken(); // Let each request carry custom token, modify according to actual situation
        }
        // GET request mapping params parameters
        if (config.method === 'get' && config.params) {
            let url = config.url + '?' + tansParams(config.params);
            url = url.slice(0, -1);
            config.params = {};
            config.url = url;
        }
        if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
            const requestObj = {
                url: config.url,
                data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
                time: new Date().getTime(),
            };
            const sessionObj = cache.session.getJSON('sessionObj');
            if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
                cache.session.setJSON('sessionObj', requestObj);
            } else {
                const s_url = sessionObj.url; // Request URL
                const s_data = sessionObj.data; // Request data
                const s_time = sessionObj.time; // Request time
                const interval = 1000; // Interval (ms), less than this time considered duplicate submission
                if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
                    const message = 'Data is being processed, please do not submit again';
                    console.warn(`[${s_url}]: ` + message);
                    return Promise.reject(new Error(message));
                } else {
                    cache.session.setJSON('sessionObj', requestObj);
                }
            }
        }
        return config;
    },
    (error) => {
        store.dispatch('loading/setLoading', false); // Hide loading status
        return Promise.reject(error);
    }
);

// Response interceptor
service.interceptors.response.use(
    (res) => {
        store.dispatch('loading/setLoading', false); // Hide loading status
        // If status code is not set, default to success
        const code = res.data.code || 200;
        // Get error message
        const msg = errorCode[code] || res.data.msg || errorCode['default'];
        // Binary data returned directly
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            return res.data;
        }
        if (code === 401) {
            if (!isRelogin.show) {
                isRelogin.show = true;
                MessageBox.confirm('Login status has expired, you can stay on this page or login again', 'System Notification', {
                    confirmButtonText: 'Relogin',
                    cancelButtonText: 'Cancel',
                    type: 'warning',
                })
                    .then(() => {
                        isRelogin.show = false;
                        console.log("Redirect to login");
                        router.push('/login'); // Use imported router instance for redirection
                    })
                    .catch(() => {
                        isRelogin.show = false;
                    });
            }
            return Promise.reject('Invalid session, or session has expired, please login again.');
        } else if (code === 500) {
            if (msg === "Insufficient user balance")
            Message({
                message: msg+", please recharge",
                type: 'error',
            });
            if (msg==="Insufficient user balance"){
                router.push('/personal-center')
            }
            return Promise.reject(new Error(msg));
        } else if (code !== 200) {
            Notification.error({
                title: msg,
            });
            return Promise.reject('error');
        } else {
            return res.data;
        }
    },
    (error) => {
        store.dispatch('loading/setLoading', false); // Hide loading status
        let { message } = error;
        if (message == 'Network Error') {
            message = 'Backend interface connection exception';
        } else if (message.includes('timeout')) {
            message = 'System interface request timeout';
        } else if (message.includes('Request failed with status code')) {
            message = 'System interface ' + message.substr(message.length - 3) + ' exception';
        }
        Message({
            message: message,
            type: 'error',
            duration: 5 * 1000,
        });
        return Promise.reject(error);
    }
);

export default service;
