import request from '../http'

export default{
    searchByPage(data){
        return request._post('/doctor/searchByPage',data)
    },
    searchById(id){
        return request._post('/doctor/searchById',{id: id})
    },
    deleteByIds(ids){
        return request._post('/doctor/deleteByIds',{ids: ids})
    },
    searchContent(id){
        return request._post('/doctor/searchContent',{id: id})
    },
    searchByDeptSubId(deptSubId){
        return request._post('/doctor/searchByDeptSubId',{deptSubId: deptSubId})
    },
    searchDeptSubSchedule(date,deptSubId){
        return request._post('/doctor/work_plan/schedule/searchDeptSubSchedule',{date: date,deptSubId: deptSubId})
    },
    updateSchedule(workPlanId, maximum, slots){
        return request._post('/doctor/work_plan/schedule/updateSchedule',{
            workPlanId: workPlanId,
            maximum: maximum,
            slots: slots
        })
    },
    searchByWorkPlanId(workPlanId){
        return request._post('/doctor/work_plan/schedule/searchByWorkPlanId',{
            workPlanId: workPlanId
        })
    },
    doctorInsertOrUpdate(url,data){
        return request._post(`/doctor/${url}`,data)
    }


}
