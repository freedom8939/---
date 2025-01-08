import request from '../http'

export default{
    login(username,password){
        return request._post('/mis_user/login',{ username: username, password: password })
    },
    logout(){
        return request._get('/mis_user/logout')
    }
}