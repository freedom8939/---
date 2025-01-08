import axios from 'axios'
import {ElMessage} from 'element-plus'
import router from '../router'

const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 10000,
    headers: {'Content-Type': 'application/json;charset=utf-8'},
});


// 请求拦截器
service.interceptors.request.use(
    config => {

        const token = localStorage.getItem("token")
        token && (config.headers.token = token)

        return config
    },
    error => {
        return Promise.error(error)
    })


// 响应拦截器
service.interceptors.response.use(
    response => {
        if (response.status === 200) {
            return Promise.resolve(response)
        } else {
            ElMessage.error({
                message: response.msg,
                duration: 1200
            });
            return Promise.reject(response)
        }
    },
    // 服务器状态码不是200的情况
    error => {
        // console.log(error)
        if (error.response.status) {
            switch (error.response.status) {
                case 500:
                    ElMessage.error({
                        message: error.response.data.error,
                        duration: 1200
                    });
                    break
                case 404:
                    ElMessage.error({
                        message: '网错错误，请稍后再试！',
                        duration: 1200
                    });
                    break
                case 401:
                    router.push('Login')
                // 其他错误，直接抛出错误提示
                default:
                    ElMessage.error({
                        message: error.response.data.message,
                        duration: 1200
                    });
            }
            return Promise.reject(error.response)
        }
    }
)


service._get = (url) => {
    return new Promise((resolve, reject) => {
        service.get(url, {})
            .then(res => {
                resolve(res.data)
            })
            .catch(err => {
                // console.log('error')
                reject(err.data)
            })
    })
}

service._post = (url, params) => {
    return new Promise((resolve, reject) => {
        service.post(url, JSON.stringify(params))
            .then(res => {
                resolve(res.data)
            })
            .catch(err => {
                reject(err.data)
            })
    })
}

export default service
