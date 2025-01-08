<template>
  <div v-if="isAuth(['ROOT', 'DOCTOR:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
      <el-form-item prop="name">
        姓名:&nbsp;
        <el-input
            v-model="dataForm.name"
            placeholder="请输入姓名"
            size="medium"
            class="input"
            clearable="clearable"
        />
      </el-form-item>
      <el-form-item>
        科室:&nbsp;
        <el-select
            v-model="dataForm.deptId"
            class="input"
            placeholder="请输入科室"
            size="medium"
            clearable="clearable"
        >
          <el-option v-for="one in medicalDeptList" :label="one.name" :value="one.id"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        学历: &nbsp;
        <el-select
            v-model="dataForm.degree"
            class="input"
            placeholder="请输入学历"
            size="medium"
            clearable="clearable"
        >
          <el-option label="博士" value="博士"/>
          <el-option label="硕士" value="硕士"/>
          <el-option label="本科" value="本科"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        职位:&nbsp;
        <el-select v-model="dataForm.job" class="input" placeholder="职位" size="medium" clearable="clearable">
          <el-option label="主任医师" value="主任医师"/>
          <el-option label="副主任医师" value="副主任医师"/>
          <el-option label="主治医师" value="主治医师"/>
          <el-option label="副主治医师" value="副主治医师"/>
        </el-select>
      </el-form-item>
      <!--            <el-form-item>
                      <el-select v-model="dataForm.recommended" class="input" placeholder="推荐级别"
                                 clearable="clearable">
                          <el-option label="priority" value="true"/>
                          <el-option label="non-priority" value="false"/>
                      </el-select>
                  </el-form-item>-->
      <el-form-item>
        <el-button size="medium" type="primary" @click="searchHandle()">查询</el-button>
        <el-button
            size="medium"
            type="primary"
            :disabled="!isAuth(['ROOT', 'DOCTOR:INSERT'])"
            @click="addHandle()"
        >
          新增医生
        </el-button>
        <el-button
            size="medium"
            type="danger"
            :disabled="!isAuth(['ROOT', 'DOCTOR:DELETE'])"
            @click="deleteHandle()"
        >
          批量删除
        </el-button>
      </el-form-item>
<!--      <div style="float: right">-->
<!--        <el-radio-group v-model="dataForm.status" @change="searchHandle()">-->
<!--          <el-radio-button label="在职"/>-->
<!--          <el-radio-button label="离职"/>-->
<!--          <el-radio-button label="退休"/>-->
<!--        </el-radio-group>-->
<!--      </div>-->
    </el-form>

    <el-table
        :data="dataList"
        border
        v-loading="dataListLoading"
        :cell-style="{ padding: '3px 0' }"
        style="width: 100%;"
        size="medium"
        @selection-change="selectionChangeHandle"
        @expand-change="expand"
        :row-key="getRowKeys"
        :expand-row-keys="expands"
        @sort-change="orderHandle"
    >


      <!--          -->
      <el-table-column type="expand">
        <template #default="scope">
          <div>
            <table class="content">
              <tr>
                <th width="140">出生日期</th>
                <td width="320">{{ content.birthday }}</td>
                <th width="140">身份证号</th>
                <td width="320">{{ content.pid }}</td>

                <td width="110" rowspan="3" align="center">
                  <el-upload
                      class="avatar-uploader"
                      :action="action"
                      :headers="{ token: token }"
                      with-credentials="true"
                      :on-success="updatePhotoSuccess"
                      :on-error="updatePhotoError"
                      :show-file-list="false"
                      :data="{ doctorId: scope.row.id }"
                  >
                    <el-image style="width: 100px; height: 100px" :src="content.photo" :fit="fit">
                      <template #error>
                        <div class="error-img">
                          <el-icon>
                            <Picture/>
                          </el-icon>
                        </div>
                      </template>
                    </el-image>
                  </el-upload>
                </td>
              </tr>
              <tr>
                <th>医生在职编号</th>
                <td>{{ content.uuid }}</td>
                <th>入职日期</th>
                <td>{{ content.hiredate }}</td>
              </tr>
              <tr>
                <th>电子邮箱</th>
                <td>{{ content.email }}</td>
                <th>备注</th>
                <td>{{ content.remark }}</td>
              </tr>
              <tr>
                <th>标签描述</th>
                <td>
                  <el-tag v-for="one of content.tag">{{ one }}</el-tag>
                </td>
                <th>家庭地址</th>
                <td colspan="2">{{ content.address }}</td>
              </tr>
              <tr>
                <th>个人介绍</th>
                <td colspan="4">{{ content.description }}</td>
              </tr>
            </table>
          </div>
        </template>
      </el-table-column>
      <!--          -->
      <el-table-column type="selection" header-align="center" align="center" width="50"/>


      <el-table-column type="index" header-align="center" align="center" width="100" label="序号">
        <template #default="scope">
          <span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column
          prop="name"
          header-align="center"
          align="center"
          min-width="120"
          label="姓名"
          :show-overflow-tooltip="true"
      />
      <el-table-column prop="sex" header-align="center" align="center" min-width="70" label="性别"/>
      <el-table-column prop="tel" header-align="center" align="center" min-width="120" label="电话"/>
      <el-table-column prop="job" header-align="center" align="center" min-width="100" label="职务"/>
      <el-table-column
          prop="deptName"
          header-align="center"
          align="center"
          min-width="120"
          label="科室"
          :show-overflow-tooltip="true"
          sortable
      />
      <el-table-column
          prop="subName"
          header-align="center"
          align="center"
          min-width="120"
          label="诊室"
          :show-overflow-tooltip="true"
      />
      <el-table-column
          prop="school"
          header-align="center"
          align="center"
          min-width="170"
          label="毕业学校"
          :show-overflow-tooltip="true"
      />
      <el-table-column prop="degree" header-align="center" align="center" min-width="100"
                       label="学历"/>
      <el-table-column prop="status" header-align="center" align="center" min-width="80" label="状态">
        <template #default="scope">
          <el-tag
              :type="scope.row.status === '在职' ? 'success' : scope.row.status === '离职' ? 'warning' : ''"
              disable-transitions
          >{{ scope.row.status }}
          </el-tag
          >
        </template>

      </el-table-column>
      <el-table-column header-align="center" align="center" width="150" label="操作">
        <template #default="scope">
          <el-button
              type="text"
              size="medium"
              :disabled="!isAuth(['ROOT', 'DOCTOR:UPDATE'])"
              @click="updateHandle(scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
              type="text"
              size="medium"
              :disabled="!isAuth(['ROOT', 'DOCTOR:DELETE'])"
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
import AddOrUpdate from './doctor-add-or-update.vue';
import {ElMessage} from "element-plus";

export default {
  components: {
    AddOrUpdate
  },
  data() {
    return {
      token: localStorage.getItem('token'),
      action: `${this.$baseUrl}/doctor/updatePhoto`,
      dataForm: {
        name: '',
        deptId: '',
        degree: '',
        job: '',
        recommended: '',
        status: '在职',
        order: null
      },
      dataList: [],
      medicalDeptList: [],
      pageIndex: 1,
      pageSize: 10,
      totalCount: 0,
      dataListLoading: false,
      dataListSelections: [],
      dataRule: {
        name: [{required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误！'}]
      },
      expands: [],
      getRowKeys(row) {
        return row.id;
      },
      content: {
        id: null,
        photo: '',
        pid: '',
        birthday: '',
        uuid: '',
        hiredate: '',
        email: '',
        remark: '',
        tag: '',
        address: '',
        description: ''
      }
    };
  },
  methods: {
    loadDateList: function () {
      this.dataListLoading = true
      const json = {'在职': 1, '离职': 2, '退休': 3}
      const data = {
        page: this.pageIndex,
        length: this.pageSize,
        name: this.dataForm.name === '' ? null : this.dataForm.name,
        deptId: this.dataForm.deptId === '' ? null : this.dataForm.deptId,
        degree: this.dataForm.degree === '' ? null : this.dataForm.degree,
        job: this.dataForm.job === '' ? null : this.dataForm.job,
        recommended: this.dataForm.recommended === '' ? null : this.dataForm.recommended,
        status: json[this.dataForm.status],
        order: this.dataForm.order
      }

      this.$api.doctor.searchByPage(data)
          .then(res => {
            let result = res.result
            let temp = {
              '1': '在职',
              '2': '离职',
              '3': '退休'
            }

            for (let one of result.list) {
              one.status = temp[one.status + '']
              // console.log(one.degree)
              if (one.degree === '研究生') one.degree = '硕士'
            }
            this.dataList = result.list;
            this.totalCount = result.totalCount
            this.dataListLoading = false;
          })
          .catch(err => {
            this.dataListLoading = false
          })
    },
    loadMedicalDeptList: function () {
      this.$api.dept.searchAll().then(res => {
        this.medicalDeptList = res.result.list
        // console.log(res)
      })
    },
    sizeChangeHandle: function (val) {
      this.pageSize = val
      this.pageIndex = 1
      this.loadDateList()
    },
    currentChangeHandle: function (val) {
      this.pageIndex = val
      this.loadDateList()
    },
    searchHandle: function () {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.$refs['dataForm'].clearValidate();
          if (this.pageIndex !== 1) {
            this.pageIndex = 1;
          }
          this.loadDateList();
        } else {
          return false;
        }
      });
    },
    orderHandle: function (param) {
      let order = param.order;
      if (order === 'ascending') {
        this.dataForm.order = 'ASC';
      } else if (order === 'descending') {
        this.dataForm.order = 'DESC';
      } else {
        return;
      }
      this.dataList = [];
      this.loadDateList();
    },
    expand: function (row, expandedRows) {
      if (expandedRows.length > 0) {
        this.expands = []
        this.expands.push(row.id)

        this.$api.doctor.searchContent(row.id)
            .then(res => {
              this.content.id = row.id
              this.content.photo = `${this.$minioUrl}${res.photo}?random=${Math.random()}`
              this.content.pid = res.pid;
              this.content.birthday = res.birthday;
              this.content.uuid = res.uuid;
              this.content.hiredate = res.hiredate;
              this.content.email = res.email;
              this.content.remark = res.remark;
              this.content.tag = res.tag;
              this.content.address = res.address;
              this.content.description = res.description;
            })

      } else {
        this.expands = []
      }
    },
    updatePhotoSuccess: function () {
      this.content.photo = `${this.$minioUrl}/doctor/doctor-${this.content.id}.jpg?random=${Math.random()}`
    },
    updatePhotoError: function () {
      ElMessage(
          {
            message: '上传图片失败！',
            type: 'error',
            duration: 1200
          })
    },
    addHandle: function () {
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init()
      })
    },
    updateHandle: function (id) {
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },
    deleteHandle: function (id) {
      let ids = id ? [id] : this.dataListSelections.map(item => {
        return item.id
      })
      console.log(this.dataListSelections)
      console.log(ids)
      if (ids.length === 0) {
        ElMessage({
          message: '未选择任何记录！',
          type: 'warning',
          duration: 1200
        });
      } else {
        this.$confirm('确定要删除所选记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$api.doctor.deleteByIds(ids).then(res => {
            ElMessage({
              message: '删除成功！',
              type: 'success',
              duration: 1200,
              onClose: () => {
                this.loadDateList();
              }
            })
          })
        })
      }
    },
    selectionChangeHandle(e) {
      this.dataListSelections = e
    }

  },
  created: function () {
    this.loadMedicalDeptList()
    this.loadDateList()
  }
};
</script>

<style lang="less" scoped="scoped">
@import url(doctor.less);
</style>
