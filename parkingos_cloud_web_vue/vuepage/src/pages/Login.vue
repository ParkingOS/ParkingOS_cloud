<!--<template>-->
<!--<div style="height:100%;overflow:visible">-->
			<!---->
			<!--<div :style="top">-->
				<!--<img src="../assets/lg1.png" width="50px" height="50px" style="display:block;padding-top:4px;padding-left:15px;float:left">-->
				<!--<div style="font-family: STXinwei;color:#008F4C;margin-left:10px;font-size:30px;postition:relative;line-height:60px;vertical-align:middle;font-weight:bold">电子支付·互联互通</div>-->
			<!--</div>-->

			<!--<div class="bg" :style="bgheight">-->
					<!---->
					<!--<div :style="content">-->
								<!---->
								<!--&lt;!&ndash;<a href="http://localhost:8080/cms-web/resource/user/static/logo3.png" style="color:black">下载接口文档</a>&ndash;&gt;-->
								<!--<div style="margin-top:60px;margin-left:50px;float:left">-->
									<!--<img src="../assets/center1.png" width="350px" >-->
								<!--</div>-->

								<!--<div  style="width:300px;float:right;position:relative;margin-top:80px;margin-right:50px">-->
								<!--<el-form :model="ruleForm" :rules="rules2" ref="ruleForm" label-position="left" label-width="0px" class="login-container">-->
									<!--<h3 class="title">账户登录</h3>-->
									<!--<el-form-item prop="account">-->
										<!--<el-input type="text" v-model="ruleForm.account" placeholder="账号"></el-input>-->
									<!--</el-form-item>-->
									<!--<el-form-item prop="checkPass">-->
										<!--<el-input type="password" v-model="ruleForm.checkPass" placeholder="密码"></el-input>-->
									<!--</el-form-item>-->
									<!--&lt;!&ndash;<el-checkbox v-model="checked" checked class="remember">记住密码</el-checkbox>&ndash;&gt;-->
									<!--<el-form-item style="width:100%;">-->
										<!--<el-button type="primary" style="width:100%;" @click.native.prevent="onSubmit" :loading="logining" >登 录</el-button>-->
										<!--<div align="right">-->
											<!--<el-button @click="handleReset" size="small" type="text" style="color:white" >忘记密码?</el-button>-->
										<!--</div>-->
									<!--</el-form-item>-->
								<!--</el-form>-->
							<!---->
								<!--</div>-->
						<!--</div>-->

				<!--<div style="clear:both;text-align:center;font-size:15px;background:#fff;padding:5px">-->
						<!--&lt;!&ndash;深圳泊链科技有限公司&ndash;&gt;&nbsp;&nbsp;&nbsp;&nbsp;粤ICP备17041952<br/>2017 bolink.club. All Rights Reserved-->
				<!--</div>-->

			<!--</div>-->

			<!--&lt;!&ndash;重置密码对话框&ndash;&gt;-->
			<!--<el-dialog-->
				<!--title="找回密码"-->
				<!--v-model="getPassVisible"-->
				<!--size="tiny"-->
				<!--@close="closeGetPass">-->
				<!--<el-form ref="passform" :model="getpass" :rules="getPassFormRules" label-width="90px" style="width:96%">-->
					 <!--<el-form-item label="用户类型" prop="user_type">-->
						<!--<el-select v-model="getpass.user_type" placeholder="请选择" style="width:100%">-->
						<!--<el-option label="厂商平台" value="2"></el-option>-->
						<!--<el-option label="服务商" value="3"></el-option>-->
						<!--<el-option label="停车场" value="4"></el-option>-->
						<!--</el-select>-->
					<!--</el-form-item>-->
					<!--<el-form-item label="登录账户" prop="userid">-->
						<!--<el-input v-model="getpass.userid"></el-input>-->
					<!--</el-form-item>-->
					<!--<el-form-item label="密保手机" prop="mobile">-->
						<!--<el-input v-model="getpass.mobile"></el-input>-->
					<!--</el-form-item>-->
					<!--<el-form-item label="验证码">-->
						<!--<el-input v-model="getpass.code" style="width:50%"></el-input>-->
						<!--<el-button  type="primary" :plain="true" @click="getckey" :disabled="codeBtn" style="color:black">{{passinfo}}</el-button>-->
					<!--</el-form-item>-->
					<!--</el-form>-->
					<!--<el-col align="right" style="margin-bottom:18px;margin-top:5px;">-->
						<!--<el-button @click="getPassVisible = false" size="small">取 消</el-button>-->
						<!--<el-button type="primary" @click="checkCode" size="small" :disabled="hasCode">确 定</el-button>-->
					<!--</el-col>-->
			<!--</el-dialog>-->

			<!--&lt;!&ndash;获取ckey对话框&ndash;&gt;-->
			<!--<el-dialog-->
				<!--title="获取验证码"-->
				<!--v-model="getckeyVisible"-->
				<!--top="25%"-->
				<!--size="tiny"-->
				<!--@close="closegetckey">-->
				<!--<el-form ref="ckeyform" :model="getckeyForm" label-width="80px" style="width:85%;margin-left:20px">-->
					<!--<el-form-item label="验证码">-->
						<!--<div class="code" >{{ckey}}</div>-->
					<!--</el-form-item>-->
					<!--<el-form-item label="请输入">-->
						<!--<el-input v-model="getckeyForm.ckey"></el-input>-->
					<!--</el-form-item>-->
					<!--</el-form>-->
					<!--<el-col align="right" style="margin-bottom:18px;margin-top:5px;">-->
						<!--<el-button @click="getckeyVisible = false" size="small">取 消</el-button>-->
						<!--<el-button type="primary" @click="reguser" size="small">确 定</el-button>-->
					<!--</el-col>-->
			<!--</el-dialog>-->

			<!--&lt;!&ndash;重置密码对话框&ndash;&gt;-->
			<!--<el-dialog-->
				<!--title="重置密码"-->
				<!--v-model="resetPassVisible"-->
				<!--top="25%"-->
				<!--size="tiny"-->
				<!--@close="closeResetPass">-->
				<!--<el-form ref="resetpassform" :model="resetPassForm" :rules="resetPassFormRules" label-width="100px" style="width:85%;margin-left:20px">-->
					<!--<el-form-item label="请输入密码" prop="pass1">-->
						<!--<el-input v-model="resetPassForm.pass1" type="password" ></el-input>-->
					<!--</el-form-item>-->
					<!--<el-form-item label="再输入密码" prop="pass2"> -->
						<!--<el-input v-model="resetPassForm.pass2" type="password"></el-input>-->
					<!--</el-form-item>-->
					<!--</el-form>-->
					<!--<el-col align="right" style="margin-bottom:18px;margin-top:5px;">-->
						<!--<el-button @click="resetPassVisible = false" size="small">取 消</el-button>-->
						<!--<el-button type="primary" @click="resetPasss" size="small">确 定</el-button>-->
					<!--</el-col>-->
			<!--</el-dialog>-->

<!--</div>-->



<!--</template>-->

<!--<script>-->
<!--import CryptoJS from 'crypto-js';-->
<!--import { path,checkPass } from '../api/api'-->
<!--import MD5 from 'crypto-js/md5'-->
<!--import common from '../common/js/common'-->
<!--var key = CryptoJS.enc.Utf8.parse("zldboink20170613");   -->
<!--var iv  = CryptoJS.enc.Utf8.parse('zldboink20170613');-->
<!--var timer;-->
<!--export default {-->
	<!--data() {-->
		<!--return {-->
			<!--logining: false,-->
			<!--getPassVisible:false,-->
			<!--getckeyVisible:false,-->
			<!--resetPassVisible:false,-->
			<!--codeBtn:false,-->
			<!--hasCode:true,-->
			<!--passinfo:'获取验证码',-->
			<!--top:'',-->
			<!--bgheight:'',-->
			<!--content:'',-->
			<!--form:'',-->
			<!--wrap:'',-->
			<!--bg:'',-->
			<!--ruleForm: {-->
			  <!--account: '',-->
			  <!--checkPass: '',-->
			<!--},-->
			<!--rules2: {-->
			  <!--account: [-->
				<!--{ required: true, message: '请输入账号', trigger: 'blur' },-->
				<!--//{ validator: validaePass }-->
			  <!--],-->
			  <!--checkPass: [-->
				<!--{ required: true, message: '请输入密码', trigger: 'blur' },-->
				<!--//{ validator: validaePass2 }-->
			  <!--]-->
			<!--},-->
			<!--checked: false,-->
			<!--getpass:{-->
				<!--user_type:'',-->
				<!--userid:'',-->
				<!--mobile:'',-->
				<!--code:''-->
			<!--},-->
			<!--getckeyForm:{-->
				<!--ckey:''-->
			<!--},-->
			<!--ckey:'',-->
			<!--resetPassForm:{-->
				<!--pass1:'',-->
				<!--pass2:''-->
			<!--},-->
			<!--token:'',-->
			<!--getPassFormRules:{-->
				<!--user_type: [-->
					<!--{ required: true, message: '请选择用户类型', trigger: 'change' }-->
				<!--],-->
				<!--userid: [-->
					<!--{ required: true, message: '请输入账户名称', trigger: 'blur' }-->
				<!--],-->
				<!--mobile: [-->
					<!--{ required: true, message: '请输入密保电话', trigger: 'blur' }-->
				<!--],-->
			<!--},-->
			<!--resetPassFormRules:{-->
				<!--pass1: [-->
					<!--{ validator:checkPass, required: true, trigger: 'blur' }-->
				<!--],-->
				<!--pass2: [-->
					<!--{ validator:checkPass, required: true, trigger: 'blur' }-->
				<!--],-->
			<!--},-->
			<!--time:''-->

		<!--}-->
	<!--},-->
	<!--mounted(){-->
		<!--//alert(common.gwh())-->
		<!--var vm=this-->
		<!--var pad = Math.ceil((common.gww()-1366)/2)-->
		<!--this.top='height:60px;padding-left:'+pad+'px;padding-right:'+pad+'px'-->
		<!--this.bgheight='height:'+(common.gwh()-110)+'px;width:'+common.gww()+'px'-->
		<!--this.content='float:left;width:1000px;height:'+(common.gwh()-110)+'px;margin-left:'+Math.ceil((common.gww()-1000)/2)+'px'-->

		<!--//检测回车按键-->
		<!--document.addEventListener("keydown",function(e){-->
			<!--if(e.keyCode==13){-->
				<!--vm.handleSubmit2()-->
			<!--}-->
		<!--},false)-->
	<!--},-->
	<!--methods: {-->
		<!--handleReset() {-->
			<!--console.log('忘记密码')-->
			<!--this.getPassVisible=true-->
		<!--},-->
		<!--closegetckey(){-->
			<!--this.getckeyForm.ckey=''-->
		<!--},-->
		<!--closeGetPass(){-->
			<!--this.getpass.code=''-->
			<!--window.clearInterval(timer)-->
			<!--this.codeBtn=false-->
			<!--this.passinfo='获取验证码'-->
			<!--this.$refs['passform'].resetFields()-->
		<!--},-->
		<!--closeResetPass(){-->
			<!--this.resetPassForm.pass1=''-->
			<!--this.resetPassForm.pass2=''-->
		<!--},-->
		<!--getckey(){-->
			<!--var vm = this-->
			<!--var cform = this.getpass-->
			<!--this.$refs.passform.validate((valid)=>{-->
				<!--if(valid){-->
					<!--vm.$.post(path+'/user/getckey',cform,function(ret){-->
						<!--if(ret.state==1){-->
							<!--vm.ckey = CryptoJS.AES.decrypt(ret.ckey,key, {iv:iv,mode:CryptoJS.mode.CBC}).toString(CryptoJS.enc.Utf8)-->
							<!--vm.getckeyVisible=true-->
						<!--}else{-->
						<!--//更新失败-->
						<!--vm.$message({-->
							<!--message: ret.errmsg,-->
							<!--type: 'error',-->
							<!--duration: 4000-->
						<!--});-->
						<!--}-->
					<!--},"json")-->
				<!--}-->
			<!--})-->

		<!--},-->
		<!--reguser(){-->
			<!--var vm = this-->
			<!--if(this.getckeyForm.ckey.length!=4){-->
 				  <!--vm.$message({-->
                    <!--message: "请输入正确的验证码",-->
                    <!--type: 'error',-->
                    <!--duration: 2000-->
                  <!--});-->
				  <!--return-->
			<!--}-->
			<!--var vm = this-->
			<!--var win = window-->
			<!--var cform = {'mobile':this.getpass.mobile,'ckey':this.getckeyForm.ckey}-->
			<!--vm.$.post(path+'/user/reguser',cform,function(ret){-->
                <!--if(ret.state==1){-->
					<!--vm.$message({-->
                    <!--message: "验证码已发送,请注意查收",-->
                    <!--type: 'success',-->
                    <!--duration: 1500-->
                  <!--});-->
				  <!--vm.getckeyVisible=false-->
				  <!--vm.hasCode=false-->
				  <!--//验证码发送成功-->
				  <!--vm.time=60-->
				  <!--vm.codeBtn = true-->
				  <!--timer = win.setInterval(vm.getCodeBtn,1000)-->
                <!--}else{-->
                  <!--//更新失败-->
                  <!--vm.$message({-->
                    <!--message: ret.errmsg,-->
                    <!--type: 'error',-->
                    <!--duration: 4000-->
                  <!--});-->
                <!--}-->
              <!--},"json")-->
		<!--},-->
		<!--getCodeBtn(){-->
			<!--if(this.time>0){-->
				<!--this.time -= 1-->
				<!--this.passinfo = this.time + '秒后重发'-->
			<!--}-->
			<!--if(this.time==0){-->
				<!--this.codeBtn=false-->
				<!--this.passinfo='获取验证码'-->
			<!--}-->

		<!--},-->
		<!--checkCode(){-->
			<!--var vm = this-->
			<!--if(this.getpass.code.length!=4){-->
 				  <!--vm.$message({-->
                    <!--message: "请输入正确的验证码",-->
                    <!--type: 'error',-->
                    <!--duration: 2000-->
                  <!--});-->
				  <!--return-->
			<!--}-->
			<!--var cform = {'mobile':this.getpass.mobile,'userid':this.getpass.userid,'code':this.getpass.code}-->
			<!--vm.$.post(path+'/user/checkcode',cform,function(ret){-->
                <!--if(ret.state==1){-->
					<!--vm.token=ret.token-->
					<!--//关闭当前对话框-->
					<!--vm.getPassVisible=false-->
					<!--//开启充值密码对话框-->
					<!--vm.resetPassVisible=true-->
                <!--}else{-->
                  <!--//更新失败-->
                  <!--vm.$message({-->
                    <!--message: ret.errmsg,-->
                    <!--type: 'error',-->
                    <!--duration: 4000-->
                  <!--});-->
                <!--}-->
              <!--},"json")-->

		<!--},-->
		<!--resetPasss(){-->
			<!--//重置密码-->
			<!--var vm = this-->
			<!--if(this.resetPassForm.pass1!=this.resetPassForm.pass2){-->
 				  <!--vm.$message({-->
                    <!--message: "两次输入密码不同",-->
                    <!--type: 'error',-->
                    <!--duration: 2000-->
                  <!--});-->
				  <!--return-->
			<!--}-->
			<!--var cform = {'passwd':this.resetPassForm.pass1,'token':this.token}-->
			<!--this.$refs.resetpassform.validate((valid)=>{-->
				<!--if(valid){-->
					<!--vm.$.post(path+'/user/resetpwd',cform,function(ret){-->
						<!--if(ret.state==1){-->
						<!--vm.$message({-->
							<!--message: "密码重置成功!",-->
							<!--type: 'success',-->
							<!--duration: 1500-->
						<!--});-->
						<!--vm.resetPassVisible=false-->
						<!--}else{-->
						<!--//更新失败-->
						<!--vm.$message({-->
							<!--message: "密码重置失败!",-->
							<!--type: 'error',-->
							<!--duration: 3000-->
						<!--});-->
						<!--}-->
					<!--},"json")-->
				<!--}-->
			<!--})-->
		<!--},-->
		<!--onSubmit(){-->
			<!--this.handleSubmit2()-->
		<!--},-->
		<!--handleSubmit2:function() {-->
			<!--var _this = this;-->
			<!--var pwd = CryptoJS.AES.encrypt(this.ruleForm.checkPass,key, {iv:iv,mode:CryptoJS.mode.CBC}).toString()-->
			<!--console.log(pwd)-->
			<!--this.$refs.ruleForm.validate((valid) => {-->
				<!--if (valid) {-->
					<!--this.logining = true;-->
					<!--var _this=this;-->
					<!--var loginParams = { 'username': this.ruleForm.account, 'password': pwd }-->

					<!--this.$.ajax({-->
						<!--url: path+"/user/dologin",-->
						<!--method: "POST",-->
						<!--//headers:{"Access-Control-Allow-Origin":"*","Access-Control-Allow-Methods":"POST,GET"},-->
						<!--data: loginParams,-->
						<!--success:function(result){-->
							<!--var ret = eval('('+result+')')-->
							<!--if(ret.state){-->
								<!--var u = ret.user;-->
								<!--console.log(u)-->
								<!--sessionStorage.setItem('user',JSON.stringify(u));-->
								<!--sessionStorage.setItem('token',ret.token)-->
								<!--if(u.roleid==1){-->
									<!--_this.$router.push({ path: '/bolinkunion'});-->
								<!--}else if(u.roleid==2){-->
									<!--_this.$router.push({ path: '/account'});-->
								<!--}else if(u.roleid==3){-->
									<!--_this.$router.push({ path: '/account'});-->
								<!--}else if(u.roleid==4){-->
									<!--_this.$router.push({ path: '/parkaccount'});-->
								<!--}-->
							<!--}else{-->
									<!--_this.logining = false;-->
									<!--_this.$message.error(ret.msg);-->
							<!--}-->
					<!--}-->
				<!--})-->
			<!--}-->
		<!--})-->
	<!--}-->
<!--}-->
<!--}-->
<!--</script>-->

<!--<style lang="scss" scoped>-->

	<!--.bg{-->
		<!--background: url('../assets/bg.png') no-repeat center;-->
		<!--background-size:cover;-->
	<!--}-->
  	<!--.login-container {-->
		<!--padding: 30px 40px 10px 40px;-->
			<!--background-color: white;-->
			<!--background-color: rgba(255,255,255,0.22);-->
			<!--box-shadow: 0 0 2px #cac6c6;-->
		<!--.title {-->
		<!--margin: 0px auto 30px auto;-->
		<!--text-align: left;-->
		<!--color: white;-->
		<!--}-->
		<!--.remember {-->
		<!--margin: 0px 0px 35px 0px;-->
		<!--}-->
	<!--}-->
	<!--.code{-->
		<!--background:url(../assets/code.png);-->
		<!--font-family:Arial;-->
		<!--font-style:italic;-->
		<!--color:blue;-->
		<!--font-size:30px;-->
		<!--border:0;-->
		<!--padding:2px 3px;-->
		<!--letter-spacing:3px;-->
		<!--font-weight:bolder;             -->
		<!--float:left;            -->
		<!--cursor:pointer;-->
		<!--width:100px;-->
		<!--height:36px;-->
		<!--line-height:36px;-->
		<!--text-align:center;-->
		<!--vertical-align:middle;-->
	<!--}-->
	<!---->
<!--</style>-->
