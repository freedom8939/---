import request from '../http'

export default{
    searchMyUserSig() {
        return request._get('/video_diagnose/searchMyUserSig')
    },
    online(){
        return request._get('/video_diagnose/online')
    },
    offline(){
        return request._get('/video_diagnose/offline')
    },
    updateOpenFlag(open,roomId){
        return request._post('/video_diagnose/updateOpenFlag', {open: open,roomId: roomId})
    },
    refresh(){
        return request._get('/video_diagnose/refreshInfo')
    },
    searchVideoDiagnoseInfo() {
        return request._get('/video_diagnose/searchVideoDiagnoseInfo')
    },
    searchImageByVideoDiagnoseId(videoDiagnoseId){
        return request._post('/video_diagnose/searchImageByVideoDiagnoseId', {videoDiagnoseId: videoDiagnoseId})
    }
}
