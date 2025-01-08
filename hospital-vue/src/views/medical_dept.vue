<template>
    <div v-if="isAuth(['ROOT', 'MEDICAL_DEPT:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
            <el-form-item prop="name">
                <el-input v-model="dataForm.name" placeholder="科室名称" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.outpatient" class="input" placeholder="科室类型" clearable="clearable">
                    <el-option label="门诊" value="true" />
                    <el-option label="非门诊" value="false" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.recommended" class="input" placeholder="推荐级别" clearable="clearable">
                    <el-option label="优先" value="true" />
                    <el-option label="非优先" value="false" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="primary" :disabled="!isAuth(['ROOT', 'MEDICAL_DEPT:INSERT'])" @click="addHandle()">
                    新增
                </el-button>
                <el-button type="danger" :disabled="!isAuth(['ROOT', 'MEDICAL_DEPT:DELETE'])" @click="deleteHandle()">
                    批量删除
                </el-button>
            </el-form-item>
        </el-form>
        <el-table
            :data="dataList"
            border
            v-loading="dataListLoading"
            @selection-change="selectionChangeHandle"
            :cell-style="{ padding: '3px 0' }"
            style="width: 100%;"
        >
            <el-table-column
                type="selection"
                :selectable="selectable"
                header-align="center"
                align="center"
                width="50"
            />
            <el-table-column type="index" header-align="center" align="center" width="100" label="序号">
                <template #default="scope">
                    <span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column
                prop="name"
                header-align="center"
                align="center"
                label="科室名称"
                min-width="170"
                :show-overflow-tooltip="true"
            />
            <el-table-column prop="outpatient" header-align="center" align="center" label="科室类型" min-width="120" />
            <el-table-column header-align="center" align="center" label="诊室数量" min-width="120">
                <template #default="scope">
                    <span>{{ scope.row.subs }}个</span>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="医生数量" min-width="120">
                <template #default="scope">
                    <span>{{ scope.row.doctors }}人</span>
                </template>
            </el-table-column>
            <el-table-column prop="recommended" header-align="center" align="center" label="优先推荐" min-width="120" />
            <el-table-column
                prop="description"
                header-align="center"
                align="left"
                label="科室介绍"
                min-width="430"
                :show-overflow-tooltip="true"
            />
            <el-table-column header-align="center" align="center" width="150" label="操作">
                <template #default="scope">
                    <el-button
                        type="text"
                        :disabled="!isAuth(['ROOT', 'MEDICAL_DEPT:UPDATE'])"
                        @click="updateHandle(scope.row.id)"
                    >
                        修改
                    </el-button>
                    <el-button
                        type="text"
                        :disabled="!isAuth(['ROOT', 'MEDICAL_DEPT:DELETE']) || scope.row.subs > 0"
                        @click="deleteHandle(scope.row.id)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
            @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle"
            :current-page="pageIndex"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            :total="totalCount"
            layout="total, sizes, prev, pager, next, jumper"
        ></el-pagination>

        <add-or-update ref="addOrUpdate" @refreshDataList="loadDataList"></add-or-update>
    </div>
</template>

<script>
import AddOrUpdate from './medical_dept-add-or-update.vue';
export default {
    components: {
        AddOrUpdate
    },
    data: function() {
        return {
            dataForm: {
                name: null,
                outpatient: null,
                recommended: null
            },
            dataList: [],
            pageIndex: 1,
            pageSize: 10,
            totalCount: 0,
            dataListLoading: false,
            dataListSelections: [],
            dataRule: {
                name: [{ required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,10}$', message: '部门名称格式错误' }]
            }
        };
    },
    methods: {
        
    },
    created: function() {
        
    }
};
</script>

<style></style>
