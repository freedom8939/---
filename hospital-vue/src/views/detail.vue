<template>
  <div class="hospital">
    <h1>{{ hospital.name }}</h1>
    <p>{{ hospital.description }}</p>
    <div class="departments">
      <h2>科室介绍</h2>
      <ul>
        <li v-for="department in hospital.departments" :key="department.id">{{ department.name }}</li>
      </ul>
    </div>
    <div class="doctors">
      <h2>医生团队</h2>
      <ul>
        <li v-for="doctor in hospital.doctors" :key="doctor.id">{{ doctor.name }}, {{ doctor.specialization }}</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      hospital: {
        name: "夕阳综合医院",
        description: "夕阳综合医院是一家领先的医疗机构，致力于为社区提供高质量的医疗服务。我们拥有经验丰富的医生和员工团队，致力于保障患者的健康和康复。",
        departments: [
          { id: 1, name: "心内科" },
          { id: 2, name: "骨科" },
          { id: 3, name: "神经科" },
          { id: 4, name: "肿瘤科" }
        ],
        doctors: [
          { id: 1, name: "张三", specialization: "心脏病专家" },
          { id: 2, name: "李四", specialization: "骨科医生" },
          { id: 3, name: "王五", specialization: "神经内科医生" },
          { id: 4, name: "赵六", specialization: "肿瘤科专家" }
        ]
      }
    };
  }
};
</script>

<style scoped>
/* 在此添加样式 */
.hospital {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.departments, .doctors {
  margin-top: 20px;
}

ul {
  list-style-type: none;
  padding: 0;
}

ul li {
  margin-bottom: 5px;
}
</style>
