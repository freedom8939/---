<template>
    <el-dialog
        :title="!dataForm.id ? '新增' : '编辑'"
        v-if="isAuth(['ROOT', 'DOCTOR:INSERT', 'DOCTOR:UPDATE'])"
        :close-on-click-modal="false"
        v-model="visible"
        width="480px"
    >
        <el-scrollbar height="500px">
            <el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="80px">
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="dataForm.name" clearable/>
                </el-form-item>
                <el-form-item label="身份证号" prop="pid">
                    <el-input v-model="dataForm.pid" clearable/>
                </el-form-item>
                <el-form-item label="性别">
                    <el-radio-group v-model="dataForm.sex">
                        <el-radio-button label="男"/>
                        <el-radio-button label="女"/>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="出生日期" prop="birthday">
                    <el-date-picker
                        v-model="dataForm.birthday"
                        type="date"
                        placeholder="选择日期"
                        :editable="false"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item label="毕业学校" prop="school">
                    <el-input v-model="dataForm.school" maxlength="50" clearable/>
                </el-form-item>
                <el-form-item label="学历">
                    <el-radio-group v-model="dataForm.degree">
                        <el-radio-button label="博士"/>
                        <el-radio-button label="研究生"/>
                        <el-radio-button label="本科"/>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="手机号码" prop="tel">
                    <el-input v-model="dataForm.tel" clearable/>
                </el-form-item>
                <el-form-item label="家庭地址" prop="address">
                    <el-input v-model="dataForm.address" maxlength="200" clearable/>
                </el-form-item>
                <el-form-item label="电子邮件" prop="email">
                    <el-input v-model="dataForm.email" clearable/>
                </el-form-item>
                <el-form-item label="职务" prop="job">
                    <el-select v-model="dataForm.job" clearable>
                        <el-option label="主任医师" value="主任医师"></el-option>
                        <el-option label="副主任医师" value="副主任医师"></el-option>
                        <el-option label="主治医师" value="主治医师"></el-option>
                        <el-option label="副主治医师"
                                   value="副主治医师"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="科室" prop="deptSub">
                    <el-cascader v-model="dataForm.deptSub" :options="dept" clearable/>
                </el-form-item>
                <el-form-item label="备注信息" prop="remark">
                    <el-input v-model="dataForm.remark" maxlength="50" clearable/>
                </el-form-item>
                <el-form-item label="介绍信息" prop="description">
                    <el-input
                        v-model="dataForm.description"
                        type="textarea"
                        :rows="6"
                        style="width:100%"
                        maxlength="350"
                        show-word-limit
                        clearable
                    />
                </el-form-item>
                <el-form-item label="入职日期" prop="hiredate">
                    <el-date-picker
                        v-model="dataForm.hiredate"
                        type="date"
                        placeholder="选择日期"
                        :editable="false"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item label="标签描述">
                    <el-input v-model="newTag" @keyup.enter="inputTagHandle" clearable/>
                    <p>
                        <el-tag
                            v-for="one in dataForm.tag"
                            closable
                            :disable-transitions="false"
                            @close="closeTagHandle(one)"
                        >
                            {{ one }}
                        </el-tag>
                    </p>
                </el-form-item>
                <el-form-item label="推荐级别">
                    <el-radio-group v-model="dataForm.recommended">
                        <el-radio-button label="Recommended"/>
                        <el-radio-button label="Regular"/>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="状态">
                    <el-radio-group v-model="dataForm.status">
                        <el-radio-button label="在职"/>
                        <el-radio-button label="离职"/>
                        <el-radio-button label="退休"/>
                    </el-radio-group>
                </el-form-item>
            </el-form>
        </el-scrollbar>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确认</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script>
import dayjs from 'dayjs';
import {ElMessage} from "element-plus";

export default {
    data: function () {
        return {
            visible: false,
            newTag: null,
            dept: [],

            dataForm: {
                id: null,
                name: null,
                pid: null,
                sex: 'Male',
                photo: null,
                birthday: null,
                school: null,
                degree: 'PHD',
                tel: null,
                address: null,
                email: null,
                job: null,
                deptSub: null,
                deptSubId: null,
                remark: null,
                description: null,
                hiredate: null,
                tag: [],
                recommended: 'Regular',
                status: '在职'
            },
            dataRule: {
                name: [
                    {required: true, message: '姓名不能为空！'},
                    {
                        pattern: '^[\\u4e00-\\u9fa5]{2,20}$',
                        message: '姓名格式不正确！'
                    }
                ],
                pid: [
                    {
                        required: true,
                        message: '身份证号不能为空！'
                    },
                    {
                        pattern:
                            '^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$',
                        message: '身份证号格式不正确！'
                    }
                ],
                birthday: [
                    {
                        required: true,
                        message: '出生日期不能为空！'
                    }
                ],
                school: [
                    {
                        required: true,
                        message: '毕业学校不能为空！'
                    }
                ],
                tel: [
                    {required: true, message: '手机号码不能为空！'},
                    {
                        pattern: '^1[1-9][0-9]{9}$',
                        message: '手机号码格式不正确！'
                    }
                ],
                address: [
                    {
                        required: true,
                        message: '家庭地址不能为空！'
                    }
                ],
                email: [
                    {
                        required: true,
                        message: '电子邮箱不能为空！'
                    },
                    {
                        pattern: '^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$',
                        message: '电子邮箱格式不正确！'
                    }
                ],
                job: [
                    {
                        required: true,
                        message: '职务不能为空！'
                    }
                ],
                deptSub: [
                    {
                        required: true,
                        message: '科室不能为空！'
                    }
                ],
                remark: [
                    {
                        required: true,
                        message: '备注信息不能为空！'
                    }
                ],
                description: [
                    {
                        required: true,
                        message: '介绍信息不能为空！'
                    }
                ],
                hiredate: [{required: true, message: '入职日期不能为空！'}]
            }
        };
    },
    methods: {
        loadDeptAndSub: function () {

            this.$api.dept.searchDeptAndSub().then(res => {
                const {result} = res
                let dept = [];
                for (let one in res.result) {

                    let array = []
                    for (let sub of result[one]) {
                        array.push({
                                value: sub.subId,
                                label: sub.subName
                            }
                        )
                    }
                    dept.push({
                        value: one,
                        label: one,
                        children: array
                    })

                }
                this.dept = dept;
            })
        },
        reset: function () {
            let dataForm = {
                id: null,
                name: null,
                pid: null,
                sex: 'Male',
                phote: null,
                degree: 'PHD',
                tel: null,
                address: null,
                email: null,
                job: null,
                deptSub: null,
                deptSubId: null,
                remark: null,
                description: null,
                tag: [],
                recommended: 'Regular',
                status: '在职'
            };
            this.dataForm = dataForm;
            this.newTag = null;
        },
        init: function (id) {

            this.reset()
            this.dataForm.id = id || 0
            this.visible = true
            this.$nextTick(() => {
                // this.$refs['dataForm'].resetFields()
                this.reset()
                this.loadDeptAndSub();

                if (this.dataForm.id) {
                    this.$api.doctor.searchById(id).then(res => {
                        let json = {
                            '1': '在职',
                            '2': '离职',
                            '3': '退休'
                        }
                        this.dataForm.name = res.name;
                        this.dataForm.pid = res.pid;
                        this.dataForm.sex = res.sex;
                        this.dataForm.birthday = res.birthday;
                        this.dataForm.school = res.school;
                        this.dataForm.degree = res.degree;
                        this.dataForm.tel = res.tel;
                        this.dataForm.address = res.address;
                        this.dataForm.email = res.email;
                        this.dataForm.job = res.job;
                        this.dataForm.remark = res.remark;
                        this.dataForm.description = res.description;
                        this.dataForm.hiredate = res.hiredate;
                        this.dataForm.recommended = res.recommended ? 'recommend' : 'not recommend';
                        this.dataForm.tag = res.tag;
                        this.dataForm.status = json[res.status + ''];
                        this.dataForm.deptSub = [res.deptName, res.deptSubId];
                    })
                    // that.$http('/doctor/searchById', 'POST', {id: id}, true, function (resp) {


                    //
                    // })
                }
            })
            // console.log('ok')
        },
        inputTagHandle: function () {
            if (this.newTag != null && this.newTag !== "") {
                if (this.dataForm.tag.includes(this.newTag)) {
                    ElMessage({
                        message: '不能添加重复的标签！',
                        type: 'warning',
                        duration: 1200
                    })
                } else {
                    this.dataForm.tag.push(this.newTag)
                    this.newTag = null
                }
            }
        },
        closeTagHandle: function (tag) {
            let i = this.dataForm.tag.indexOf(tag)
            this.dataForm.tag.splice(i, 1)
        },
        dataFormSubmit: function () {

            this.$refs['dataForm'].validate(function (valid) {
                console.log(valid)
                if (valid) {

                    this.dataForm.deptSubId = this.dataForm.deptSub[1];
                    let json = {
                        '在职': '1',
                        '离职': '2',
                        '退休': '3'
                    };
                    let data = {
                        id: this.dataForm.id,
                        name: this.dataForm.name,
                        pid: this.dataForm.pid,
                        sex: this.dataForm.sex,
                        birthday: this.dataForm.birthday,
                        school: this.dataForm.school,
                        // degree: that.dataForm.degree,
                        degree: 'PHD',
                        tel: this.dataForm.tel,
                        address: this.dataForm.address,
                        email: this.dataForm.email,
                        job: this.dataForm.job,
                        remark: this.dataForm.remark,
                        description: this.dataForm.description,
                        hiredate: dayjs(this.dataForm.hiredate).format('YYYY-MM-DD'),
                        tag: this.dataForm.tag,
                        recommended: this.dataForm.recommended === 'Recommended' ? 1 : 2,
                        status: json[this.dataForm.status],
                        subId: this.dataForm.deptSubId
                    }

                    this.$api.doctor.doctorInsertOrUpdate(`${!this.dataForm.id ? 'insert' : 'update'}`,data).then(res => {
                        ElMessage({
                            message: '操作成功！!',
                            type: 'success'
                        });
                        this.visible = false
                        this.$emit('refreshDataList');
                        console.log(res)
                    })
                }
            })
        }

    }
};
</script>

<style lang="less" scoped="scoped"></style>
