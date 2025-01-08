<template>
	<div class="page">
		<div class="logoimage">
			<img src="../../static/logos.png" alt="">
			<span>一款基于SpringBoot+Vue3的企业级医疗解决方案</span>
		</div>
		<el-row type="flex" justify="start" align="middle" class="container">
			<el-col :lg="14" :xl="10" class="hidden-md-and-down">
				<el-row :gutter="12" class="panel">
					<!-- <el-col :span="12">
                        <div class="left">
                            <img src="../assets/login/hospital-management-system-logo-dark.png" class="logo"/>
                            <img src="../assets/login/big-1.png" class="big"/>
                        </div>
                    </el-col> -->
					<el-col :span="12">

						<div class="right">
							<div class="title-container" style="white-space:nowrap;">
								<h2>
									<div style="margin: 5px 0;">登录到</div>
									<div>蓝海后台管理系统</div>
								</h2>

							</div>
							<div v-if="!qrCodeVisible">
								<div class="row">
									<el-input v-model="username" placeholder="请输入账号" prefix-icon="user" size="large"
										clearable></el-input>
								</div>
								<div class="row">
									<el-input type="password" v-model="password" placeholder="请输入密码" prefix-icon="Lock"
										size="large" clearable></el-input>
								</div>
								<div class="rememberAccount">
									<div style="cursor: pointer;">
										<input type="checkbox"> <span>记住账号</span>
									</div>

									<span style="color: #0052D9; cursor: pointer;">忘记账号</span>

								</div>

								<div class="row">
									<el-button class="btn" size="large" @click="login">
										登录
									</el-button>
								</div>

								<div style="display: flex;">
									<div class="row"><a class="link" @click="">二维码登录</a></div>
									<span style="margin: 0 20px;">|</span>
									<div class="row"><a class="link" @click="">手机号登陆</a></div>
								</div>

							</div>
							<div v-if="qrCodeVisible">
							</div>

							    <el-col :span="24"><div class="copyright">
								CopyRight: 2024年4C大赛
							</div></el-col>


						</div>

					</el-col>

				</el-row>




			</el-col>
			<div class="showImg">
				<img src="../../static/实验讲解.png" alt="" >
			</div>
		</el-row>
	</div>
</template>

<script>
	import {
		isUsername,
		isPassword
	} from '../utils/validate.js';
	import router from '../router/index.js';
	import {
		ElMessage,
		ElNotification
	} from "element-plus";

	export default {
		data: function() {
			return {
				username: '',
				password: '',
				qrCodeVisible: false,
				qrCode: '',
				uuid: null,
				qrCodeTimer: null,
				loginTimer: null
			};
		},

		methods: {
			login: function() {
				if (!isUsername(this.username)) {
					ElMessage({
						message: "用户名格式不正确!",
						type: "warning",
						duration: 1200
					});
				} else if (!isPassword(this.password)) {
					ElMessage({
						message: "密码格式错误!",
						type: "warning",
						duration: 1200
					});
				} else {
					this.$api.admin.login(this.username, this.password).then(res => {
						if (res.result) {
							let permissions = res.permissions;
							let token = res.token;
							localStorage.setItem("permissions", permissions);
							localStorage.setItem("token", token);

							router.push({
								name: "Home"
							});
							ElNotification({
								title: '用户登录',
								message: '登录成功，欢迎回来！',
								type: 'success',
							})
						} else {
							ElMessage({
								message: "账号或密码错误!",
								type: "error",
								duration: 1200
							});
						}
					})
				}
			}
		}

	};
</script>

<style lang="less" scoped="scoped">
	@import url('login.less');
</style>
