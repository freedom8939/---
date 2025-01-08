<template>
    <el-dialog
        :title="dataForm.workPlanId == null?'新增':'修改'"
        v-if="isAuth(['ROOT', 'SCHEDULE:INSERT', 'SCHEDULE:UPDATE'])"
        :close-on-click-modal="false"
        v-model="visible"
        width="550px"
    >
        <el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="80px">
            <el-form-item label="出诊医生" prop="doctorId">
                <el-select v-model="dataForm.doctorId" :disabled="dataForm.workPlanId != null">
                    <el-option v-for="one in doctorList" :label="one.name" :value="one.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="出诊时间">
                <div style="width: 100%;">
                    <el-checkbox v-model="checkAll" @change="checkAllChangeHandle">全选</el-checkbox>
                </div>
                <div style="width: 100%;">
                    <el-checkbox-group v-model="checkedSlot">
                        <el-checkbox
                            v-for="(one, index) in slotList"
                            :label="one"
                            :disabled="analyseCheckBoxDisable(index + 1)"
                        />
                    </el-checkbox-group>
                </div>
            </el-form-item>
<!--            <el-form-item label="时段人数">
                <el-slider
                    v-model="dataForm.slotMaximum"
                    :min="1"
                    :max="10"
                    show-input
                    :disabled="dataForm.workPlanId != null"
                />
            </el-form-item>-->
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script>
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
export default {
    data: function() {
        return {
            visible: false,
            doctorList: [],
            checkAll: false,
            slotList: [
                '08:00~08:30',
                '08:30~09:00',
                '09:00~09:30',
                '09:30~10:00',
                '10:00~10:30',
                '10:30~11:00',
                '11:00~11:30',
                '11:30~12:00',
                '13:00~13:30',
                '13:30~14:00',
                '14:00~14:30',
                '14:30~15:00',
                '15:00~15:30',
                '16:00~16:30',
                '16:30~17:00'
            ],
            checkedSlot: [],
            oldSlots: [],
            dataForm: {
                workPlanId: null,
                deptSubId: null,
                doctorId: null,
                date: new dayjs().format('YYYY-MM-DD'),
                slots: [],
                slotMaximum: 3
            },
            dataRule: {
                doctorId: [
                    {
                        required: true,
                        message: '出诊医生不能为空'
                    }
                ]
            }
        };
    },
    methods: {
        loadDoctorList: function (){
            this.$api.doctor.searchByDeptSubId(this.dataForm.deptSubId).then(res => {
                this.doctorList = res.result
            })
        },
        reset: function () {
            this.checkAll = false
            this.checkedSlot = []
            this.oldSlots = []
            let dataForm = {
                deptSubId: null,
                doctorId: null,
                date: new dayjs().format('YYYY-MM-DD'),
                slots: [],
                slotMaximum: 3
            }
            this.dataForm = dataForm
        },
        init: function (workPlandId, deptSubId, date) {
            this.reset()
            this.dataForm.workPlanId = workPlandId
            this.dataForm.deptSubId = deptSubId
            this.dataForm.date = date
            this.visible = true
            this.$nextTick(() => {
                this.$refs['dataForm'].resetFields()
                this.loadDoctorList()
                if(workPlandId !== null){
                    this.$api.doctor.searchByWorkPlanId(workPlandId).then(res => {
                        const { result } = res
                        this.dataForm.doctorId = result.doctorId
                        this.dataForm.slotMaximum = result.maximum
                        this.oldSlots = result.slots
                        console.log(this.oldSlots)
                        for(let one of result.slots){
                            this.checkedSlot.push(this.slotList[one.slot - 1])

                        }
                        // console.log(this.oldSlots)
                    })
                }
            })
        },
        checkAllChangeHandle: function (val) {
            this.checkedSlot = val ? this.slotList : []
        },
        analyseCheckBoxDisable: function (slot) {

        },
        dataFormSubmit: function () {
            this.$refs['dataForm'].validate(valid => {
                if(valid){
                    if(this.checkedSlot.length === 0){
                        ElMessage({
                            message: '出诊时间段没有选择',
                            type: 'warning'
                        })
                        return
                    }

                    if(this.dataForm.workPlanId === null){
                        this.dataForm.slots.length = 0
                        for(let one of this.checkedSlot){
                            let index = this.slotList.indexOf(one) + 1
                            this.dataForm.slots.push(index)
                        }

                        let data = {
                            deptSubId: this.dataForm.deptSubId,
                            doctorId: this.dataForm.doctorId,
                            date: this.dataForm.date,
                            slotMaximum: this.dataForm.slotMaximum,
                            totalMaximum: this.checkedSlot.length * this.dataForm.slotMaximum,
                            slots: this.dataForm.slots
                        }

                        this.$api.dept.workPlanInsert(data).then(res => {
                            let msg = res.msg

                            if(msg === 'success'){
                                ElMessage({
                                    message: '操作成功！',
                                    type: 'success'
                                })
                                this.visible = false
                                this.$emit('refreshDataList')
                            }else {
                                ElMessage({
                                    message: msg,
                                    type: 'warning'
                                })
                            }
                        })
                    }else {
                        this.dataForm.slots.length = 0
                        // console.log(this.checkedSlot)

                        for(let one of this.checkedSlot){

                            let index = this.slotList.indexOf(one) + 1
                            this.dataForm.slots.push(index)
                        }



                        let array = []
                        for(let one of this.dataForm.slots){
                            let temp = this.oldSlots.find(old => {
                                return old.slot === one
                            })


                            if(typeof temp === 'undefined'){
                                array.push({
                                    scheduleId: null,
                                    slot: one,
                                    maximum: this.dataForm.slotMaximum,
                                    operate: 'insert'
                                })
                            }
                        }

                        for(let old of this.oldSlots) {
                            let temp = this.dataForm.slots.find(one => {
                                return old.slot === one
                            })

                            if(typeof temp === 'undefined'){
                                array.push({
                                    scheduleId: old.scheduleId,
                                    slot: old.slot,
                                    maximum: this.dataForm.slotMaximum,
                                    operate: 'delete'
                                })

                            }
                        }


                        if(array.length === 0){
                            ElMessage({
                                message: '请改动出诊日程',
                                type: 'warning',
                                duration: 1200
                            })
                            return
                        }
                        const workPlanId = this.dataForm.workPlanId
                        const maximum = this.checkedSlot.length * this.dataForm.slotMaximum
                        const slots = array


                        this.$api.doctor.updateSchedule(workPlanId, maximum, slots).then(res => {
                            ElMessage({
                                message: '操作成功！',
                                type: 'success'
                            })
                            this.visible = false
                            this.$emit('refreshDataList')

                        })
                    }
                }
            })
        }
    }
};
</script>

<style lang="less" scoped="scoped"></style>
