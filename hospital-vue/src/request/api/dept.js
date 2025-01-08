import request from '../http'

export default{
    searchAll(){
        return request._get('/medical/dept/searchAll')
    },
    searchDeptAndSub(){
        return request._get('/medical/dept/searchDeptAndSub')
    },
    searchWorkPlanInRange(data){
        return request._post('/medical/dept/sub/work_plan/searchWorkPlanInRange',data)
    },
    workPlanInsert(data){
        return request._post('/medical/dept/sub/work_plan/insert',data)
    },
    deleteWorkPlan(workPlanId){
        return request._post('/medical/dept/sub/work_plan/deleteWorkPlan',{workPlanId: workPlanId})
    }



}