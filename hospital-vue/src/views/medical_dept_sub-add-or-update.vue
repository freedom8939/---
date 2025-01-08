<template>
    <el-dialog
        :title="!dataForm.id ? '新增' : '修改'"
        v-if="isAuth(['ROOT', 'MEDICAL_DEPT_SUB:INSERT', 'MEDICAL_DEPT_SUB:UPDATE'])"
        :close-on-click-modal="false"
        v-model="visible"
        width="450px"
    >
        <el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="80px">
            <el-form-item label="诊室名称" prop="name">
                <el-input v-model="dataForm.name" style="width:100%" clearable />
            </el-form-item>
            <el-form-item label="隶属科室" prop="deptId">
                <el-select v-model="dataForm.deptId" class="input" placeholder="选择科室" clearable="clearable">
                    <el-option v-for="one in deptList" :label="one.name" :value="one.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="诊室地址" prop="location">
                <el-input
                    v-model="dataForm.location"
                    type="textarea"
                    :rows="2"
                    style="width:100%"
                    maxlength="50"
                    show-word-limit
                    clearable
                />
            </el-form-item>
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
export default {
    data: function() {
        return {
            visible: false,
            deptList: [],
            dataForm: {
                id: null,
                name: null,
                deptId: null,
                location: null
            },
            dataRule: {
                name: [{ required: true, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,10}$', message: '诊室名称格式错误' }],
                deptId: [{ required: true, message: '选择科室' }],
                location: [{ required: true, message: '填写诊室地址' }]
            }
        };
    },

    methods: {
        
    }
};
</script>

<style lang="less" scoped="scoped"></style>
