<template>
    <div v-if="isAuth(['ROOT', 'SCHEDULE:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
            <el-form-item>
                <el-select v-model="dataForm.deptId" class="input" placeholder="选择科室" clearable="clearable">
                    <el-option v-for="one in deptList" :label="one.name" :value="one.id"/>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-date-picker
                    v-model="dataForm.startDate"
                    type="date"
                    placeholder="选择日期"
                    :editable="false"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                    :clearable="false"
                />
            </el-form-item>
            <el-form-item prop="doctorName">
                <el-input v-model="dataForm.doctorName" placeholder="医生姓名" class="input" clearable="clearable"/>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="primary" :disabled="!isAuth(['ROOT', 'SCHEDULE:INSERT'])" @click="addHandle()">
                    新增
                </el-button>
            </el-form-item>
        </el-form>
        <el-table
            :data="dataList"
            border
            v-loading="dataListLoading"
            :cell-style="{ padding: '7px 0' }"
            :header-cell-style="{ 'background-color': '#F5F7FA' }"
            style="width: 100%;"
        >
<!--        #e0e0e0-->
            <el-table-column type="index" header-align="center" align="center" width="100" label="序号">
                <template #default="scope">
                    <span>{{ scope.$index + 1 }}</span>
<!--                    <span>{{ (pageIndex - 1) * pageSize +  scope.$index + 1 }}</span>-->
                </template>
            </el-table-column>
            <el-table-column
                prop="deptSubName"
                header-align="center"
                align="center"
                label="诊室名称"
                min-width="170"
                :show-overflow-tooltip="true"
            />
            <el-table-column header-align="center" align="center" :label="dateList[0]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[0].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[0].date)"
                    >
                        {{ scope.row.plan[0].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[1]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[1].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[1].date)"
                    >
                        {{ scope.row.plan[1].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[2]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[2].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[2].date)"
                    >
                        {{ scope.row.plan[2].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[3]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[3].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[3].date)"
                    >
                        {{ scope.row.plan[3].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[4]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[4].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[4].date)"
                    >
                        {{ scope.row.plan[4].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[5]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[5].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[5].date)"
                    >
                        {{ scope.row.plan[5].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" :label="dateList[6]" min-width="170">
                <template #default="scope">
                    <div
                        class="content"
                        :class="scope.row.plan[6].doctors.length > 3 ? 'alignStyle' : ''"
                        @dblclick="viewWorkPlanHandle(scope.row.deptName, scope.row.deptSubId, scope.row.plan[6].date)"
                    >
                        {{ scope.row.plan[6].doctors.join('，') }}
                    </div>
                </template>
            </el-table-column>
        </el-table>
        <add ref="add" @refreshDataList="loadDataList"></add>
    </div>
</template>

<script>
import dayjs from 'dayjs';
import Add from './medical_dept_sub_work_plan-add.vue';

export default {
    components: {
        Add
    },
    data: function () {
        return {
            dept: [],
            dataForm: {
                doctorName: null,
                deptId: null,
                startDate: dayjs().format('YYYY-MM-DD'),
                endDate: dayjs()
                    .add(7, 'day')
                    .format('YYYY-MM-DD')
            },
            deptList: [],
            dataList: [],
            dateList: [],
            dataListLoading: false,
            dataRule: {
                doctorName: [{required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$', message: '姓名格式错误'}]
            }
        };
    },
    methods: {
        loadDeptList: function() {
            this.$api.dept.searchAll().then(res => {
                this.deptList = res.result.list;
                // console.log(this.dataList)

            })
        },
        loadDataList: function () {
            const data = {
                deptId: this.dataForm.deptId,
                doctorName: this.dataForm.doctorName,
                startDate: this.dataForm.startDate,
                endDate: this.dataForm.endDate
            }

            this.$api.dept.searchWorkPlanInRange(data).then(res => {
                this.dataList = res.result
                this.dateList = res.dateList
            })

        },
        searchHandle: function () {
            this.$refs['dataForm'].validate(valid => {
                if(valid) {
                    this.$refs['dataForm'].clearValidate()
                    if(this.dataForm.doctorName === '')
                        this.dataForm.dataForm = null

                    if(this.dataForm.deptId === '')
                        this.dataForm.deptId = null

                    this.dataForm.endDate = dayjs(this.dataForm.startDate)
                        .add(7,'day')
                        .format('YYYY-MM-DD')

                    this.loadDataList()

                }else {
                    return false;
                }
            })
        },
        addHandle: function () {
            this.$nextTick(() => {
                this.$refs.add.init()
            })
        }

    },
    created: function () {
        this.loadDeptList()
        this.loadDataList()

    }
};
</script>

<style lang="less" scoped="scoped">
@import url('medical_dept_sub_work_plan.less');
</style>
