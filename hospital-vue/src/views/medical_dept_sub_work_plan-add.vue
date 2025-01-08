<template>
	<el-dialog
		title="新增"
		v-if="isAuth(['ROOT', 'SCHEDULE:INSERT'])"
		:close-on-click-modal="false"
		v-model="visible"
		width="550px"
	>
		<el-scrollbar height="500px">
			<el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="80px">
				<el-form-item label="科室部门" prop="deptSub">
					<el-cascader
						v-model="dataForm.deptSub"
						:options="dept"
						style="width:60%"
						@change="deptSubChangeHandle"
					/>
				</el-form-item>
				<el-form-item label="出诊医生" prop="doctorId">
					<el-select v-model="dataForm.doctorId">
						<el-option v-for="one in doctorList" :label="one.name" :value="one.id"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item label="出诊日期" prop="date">
					<el-date-picker
						v-model="dataForm.date"
						type="date"
						placeholder="选择日期"
						:editable="false"
						format="YYYY-MM-DD"
						value-format="YYYY-MM-DD"
						style="width: 100%;"
						:clearable="false"
					/>
				</el-form-item>
				<el-form-item label="出诊时间">
					<div style="width: 100%;">
						<el-checkbox v-model="checkAll" @change="checkAllChangeHandle">全选</el-checkbox>
					</div>
					<div style="width: 100%;">
						<el-checkbox-group v-model="checkedSlot">
							<el-checkbox v-for="(one, index) in slotList" :label="one" />
						</el-checkbox-group>
					</div>
				</el-form-item>
<!--				<el-form-item label="时段人数">
					<el-slider v-model="dataForm.slotMaximum" :min="1" :max="10" show-input />
				</el-form-item>-->
			</el-form>
		</el-scrollbar>
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
            dept: [],
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

			dataForm: {
				deptSub: null,
				deptSubId: null,
				doctorId: null,
				date: new dayjs().format('YYYY-MM-DD'),
				slots: [],
				slotMaximum: 3
			},
			dataRule: {
				deptSub: [
					{
						required: true,
						message: '科室部门不能为空'
					}
				],
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
        init: function () {
            this.reset()
            this.visible = true
            this.$nextTick(() => {
                this.$refs['dataForm'].resetFields()
                this.loadDeptAndSub()

            })
        },
        reset: function () {
            this.checkALl = false
            this.checkSlot = []
            const dataForm = {
                deptSub: null,
                deptSubId: null,
                doctorId: null,
                date: new dayjs().format('YYYY-MM-DD'),
                slots: [],
                slotMaximum: 3
            }
            this.dataForm = dataForm
        },
        loadDeptAndSub: function () {
            this.$api.dept.searchDeptAndSub().then(res => {
                let { result } = res
                let dept = []
                for(let one in result) {
                    let array = []
                    for(let sub of result[one]){
                        array.push({
                            value: sub.subId,
                            label: sub.subName
                        })
                    }

                    dept.push({
                        value: one,
                        label: one,
                        children: array

                    })

                }
                this.dept = dept

            })

        },
        deptSubChangeHandle: function (e) {
            this.dataForm.deptSubId = e[1]

            this.$api.doctor.searchByDeptSubId(this.dataForm.deptSubId).then(res => {
                this.doctorList = res.result
                this.dataForm.doctorId = null;


            })

        },
        checkAllChangeHandle: function (val) {
            this.checkedSlot = val ? this.slotList : []

        },
        dataFormSubmit: function () {
            this.$refs['dataForm'].validate(valid => {
                if(valid)
                    if(this.checkedSlot.length === 0){
                        ElMessage({
                            message: '出诊时间段没有选择',
                            type: 'warning'
                        })
                        return
                    }

                this.dataForm.slots.length = 0
                for(let one of this.checkedSlot){
                    let index = this.slotList.indexOf(one) + 1
                    this.dataForm.slots.push(index)

                }

                const data = {
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
                            message: '操作成功',
                            type: 'success'
                        })
                        this.visible = false
                        this.$emit('refreshDataList')
                    }else {
                        ElMessage({
                            message: result,
                            type: 'warning'
                        })
                    }
                })
            })
        }

	}
};
</script>

<style lang="less" scoped="scoped"></style>
