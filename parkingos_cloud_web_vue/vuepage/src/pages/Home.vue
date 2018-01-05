<!--<template>-->
	<!--<el-row class="container">-->
		<!--<el-col :span="24" class="header">-->
			<!--&lt;!&ndash;-->
			<!--<el-col :span="10" class="logo" :class="collapsed?'logo-collapse-width':'logo-width'" style="text-align: center">-->
				<!--{{collapsed?'':sysName}}-->
			<!--</el-col>-->
			<!--&ndash;&gt;-->
			<!--<el-col :span="17" >-->
				<!--<img src="../assets/lg3.png" style="padding-top:0px;padding-left:8px;float:left" width="50px" height="50px"></img>-->
				<!--<div style="margin-left:5px;font-size:30px;postition:relative;line-height:50px;vertical-align:middle;float:left;font-family:STXinwei">电子支付·互联互通</div>-->
			<!--</el-col>-->
			 <!---->
			<!--&lt;!&ndash;-->
			<!--<el-col :span="10" class="tools">-->
			<!---->
				<!--<div class="tools" @click.prevent="collapse">-->
					<!--<i class="el-icon-d-arrow-left" v-show="left"></i>-->
					<!--<i class="el-icon-d-arrow-right" v-show="right"></i>-->
				<!--</div>-->
			<!---->
			<!--</el-col>&ndash;&gt;-->

			<!--<el-col :span="7" style="padding-right:10px">-->
				<!--<div style="color:#fff;font-size:15px;display:inline;right:235px;position:absolute">{{nickname}}: {{sysUserName}}</div>-->
				<!--<el-menu style="background:#008F4C;height:50px" :default-active="active" theme="dark" class="el-menu-demo" mode="horizontal"  @select="selectTop" >-->
					<!--<el-menu-item index="/securitycenter"><span style="color:#fff;font-size:15px">安全中心</span></el-menu-item>-->
					<!--<el-menu-item index="/login"><span style="color:#fff;font-size:13px">退出登录</span></el-menu-item>-->

					<!--&lt;!&ndash;<el-submenu index="2">-->
						<!--<template slot="title"><span style="color:#fff;font-size:15px">设置</span></template>-->
						<!--<el-menu-item  index="/login" style="text-align:center" >退出登录</el-menu-item>-->
					<!--</el-submenu>&ndash;&gt;-->
				<!--</el-menu>-->
				<!---->
				<!--&lt;!&ndash;-->
				<!--<el-dropdown :hide-on-click="false">-->
					<!--<span class="el-dropdown-link userinfo-inner">{{nickname}}: {{sysUserName}}</span>-->
					<!--<el-dropdown-menu slot="dropdown">-->
						<!--<el-dropdown-item>安全中心</el-dropdown-item>-->
						<!--<el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>-->
					<!--</el-dropdown-menu>-->
				<!--</el-dropdown>-->
				<!--&ndash;&gt;-->
			<!--</el-col>-->

		<!--</el-col>-->
		<!--<el-col :span="24" class="main">-->
			<!--<aside :class="collapsed?'menu-collapsed':'menu-expanded'">-->
				<!---->
				<!--&lt;!&ndash;厂商平台导航菜单&ndash;&gt;-->
				<!--<el-menu :default-active="active" class="el-menu-vertical-demo" @open="handleopen" @close="handleclose" @select="handleselect"-->
					 <!--unique-opened v-show="!collapsed">-->
					<!---->
					<!--<el-row v-show="platform">-->
						<!--<el-menu-item index="/account"><span class="menuitem">我的账户</span></el-menu-item>-->
						<!--<el-menu-item index="/server"><span class="menuitem">服务商管理</span></el-menu-item>-->
						<!--<el-menu-item index="/park"><span class="menuitem">车场管理</span></el-menu-item>-->
						<!--<el-menu-item index="/user"><span class="menuitem">会员管理</span></el-menu-item>-->
						<!--<el-menu-item v-show="false" index="/securitycenter"><span class="menuitem">会员管理</span></el-menu-item>-->
						<!--<el-submenu index="/order">-->
							<!--<template slot="title"><span class="menuitem">交易中心</span></template>-->
							<!--<el-menu-item index="/order/orderin">交易订单</el-menu-item>-->
							<!--<el-menu-item index="/query/queryin">余位调用</el-menu-item>-->
							<!--<el-menu-item index="/moneyrecord">资金流水</el-menu-item>-->
						<!--</el-submenu>-->
						<!--<el-menu-item index="/dev"><span class="menuitem">开发配置</span></el-menu-item>-->
					<!--</el-row>-->

					<!--<el-row v-show="bolink">-->
						<!--<el-menu-item index="/bolinkunion"><span class="menuitem">厂商平台管理</span></el-menu-item>-->
						<!--<el-menu-item index="/bolinkserver"><span class="menuitem">服务商管理</span></el-menu-item>-->
						<!--<el-menu-item index="/bolinkpark"><span class="menuitem">车场管理</span></el-menu-item>-->
						<!--<el-menu-item index="/bolinkuser"><span class="menuitem">会员管理</span></el-menu-item>-->
						<!--<el-menu-item index="/bolinkbusiness"><span class="menuitem">合作商家管理</span></el-menu-item>-->
						<!--<el-submenu index="/">-->
							<!--<template slot="title"><span class="menuitem">统计分析</span></template>-->
							<!--<el-menu-item index="/analysisoverview">电子交易日报</el-menu-item>-->
							<!--<el-menu-item index="/parkoverview">车场日报</el-menu-item>-->
							<!--<el-menu-item index="/tcpindex">SDK状态统计</el-menu-item>-->
							<!--&lt;!&ndash;<el-menu-item index="/tcpprofile">tcp统计2</el-menu-item>&ndash;&gt;-->
						<!--</el-submenu>-->
							<!---->
					<!--</el-row>-->

					<!--<el-row v-show="park">-->
						<!--<el-menu-item index="/parkaccount"><span class="menuitem">我的账户</span></el-menu-item>-->
						<!--<el-menu-item index="/parktrade"><span class="menuitem">交易订单</span></el-menu-item>-->
						<!--<el-menu-item index="/daily"><span class="menuitem">车场日报</span></el-menu-item>-->
						<!--<el-submenu v-if="true" index="/">-->
							<!--<template slot="title"><span class="menuitem">自助缴费机</span></template>-->
							<!--<el-menu-item index="/centerpayment">设备管理</el-menu-item>-->
							<!--<el-menu-item index="/centerpaymentaccount">缴费机明细</el-menu-item>-->
						<!--</el-submenu>-->
						<!--<el-menu-item index="/withdraws"><span class="menuitem">账户明细</span></el-menu-item>-->
						<!--<el-menu-item index="/Zpark"><span class="menuitem">zPark</span></el-menu-item>-->
					<!--</el-row>-->

					<!--<el-row v-show="server">-->
						<!--<el-menu-item index="/account"><span class="menuitem">我的账户</span></el-menu-item>-->
						<!--<el-menu-item index="/serverpark"><span class="menuitem">车场管理</span></el-menu-item>-->
						<!--<el-submenu index="/order">-->
							<!--<template slot="title"><span class="menuitem">交易中心</span></template>-->
							<!--<el-menu-item index="/order/orderin">交易订单</el-menu-item>-->
							<!--<el-menu-item index="/query/queryin">余位调用</el-menu-item>-->
							<!--<el-menu-item index="/moneyrecord">资金流水</el-menu-item>-->
						<!--</el-submenu>-->
					<!--</el-row>-->
				<!--</el-menu>-->
				<!--&lt;!&ndash;导航菜单-折叠后&ndash;&gt;-->
				<!--&lt;!&ndash;-->
				<!--<ul class="el-menu el-menu-vertical-demo collapsed" v-show="collapsed" ref="menuCollapsed">-->
					<!--<li class="el-submenu item">-->
					<!--<div class="el-submenu__title" style="padding-left: 20px;"><i :class="el-icon-message"></i></div>-->
					<!--</li>-->
				<!--</ul>-->
				<!---->
				<!--<ul class="el-menu el-menu-vertical-demo collapsed" v-show="collapsed" ref="menuCollapsed">-->
					<!--<li v-for="(item,index) in $router.options.routes" v-if="!item.hidden" class="el-submenu item">-->
						<!--<template v-if="!item.leaf">-->
							<!--<div class="el-submenu__title" style="padding-left: 20px;" @mouseover="showMenu(index,true)" @mouseout="showMenu(index,false)"><i :class="item.iconCls"></i></div>-->
							<!--<ul class="el-menu submenu" :class="'submenu-hook-'+index" @mouseover="showMenu(index,true)" @mouseout="showMenu(index,false)">-->
								<!--<li v-for="child in item.children" v-if="!child.hidden" class="el-menu-item" style="padding-left: 40px;" :class="$route.path==child.path||$route.path=='/out'?'is-active':''" @click="$router.push(child.path)">{{child.name}}</li>-->
							<!--</ul>-->
						<!--</template>-->
						<!--<template v-else>-->
							<!--<li class="el-submenu">-->
								<!--<div class="el-submenu__title el-menu-item" style="padding-left: 20px;height: 56px;line-height: 56px;padding: 0 20px;" :class="$route.path==item.children[0].path||$route.path=='/out'?'is-active':''" @click="$router.push(item.children[0].path)"><i :class="item.iconCls"></i></div>-->
							<!--</li>-->
						<!--</template>-->
					<!--</li>-->
				<!--</ul>-->
				<!--&ndash;&gt;-->
				<!---->
			<!--</aside>-->
			<!--<section class="content-container">-->
				<!--<div class="grid-content bg-purple-light">-->
					<!--<el-col :span="24" class="content-wrapper">-->
						<!--<keep-alive>-->
							<!--<router-view></router-view>-->
						<!---->
						<!--</keep-alive>-->
					<!--</el-col>-->
				<!--</div>-->
			<!--</section>-->
		<!--</el-col>-->
	<!--</el-row>-->
<!--</template>-->

<!--<script>-->
<!--import { path } from '../api/api'-->
	<!--export default {-->
		<!--data() {-->
			<!--return {-->
				<!--active:'',-->
				<!--bolink:false,-->
				<!--park:false,-->
				<!--platform:false,-->
				<!--left:true,-->
				<!--right:false,-->
				<!--sysName:'联盟管理后台',-->
				<!--server:'',-->
				<!--collapsed:false,-->
				<!--sysUserName: '',-->
				<!--nickname: '',-->
				<!--user:'',-->
				<!--form: {-->
					<!--name: '',-->
					<!--region: '',-->
					<!--date1: '',-->
					<!--date2: '',-->
					<!--delivery: false,-->
					<!--type: [],-->
					<!--resource: '',-->
					<!--desc: ''-->
				<!--},-->
				<!--secureVisible:false,-->
			<!--}-->
		<!--},-->
		<!--methods: {-->
			<!--openSecurity(){-->
				<!--this.active = '/securitycenter'-->
				<!--console.log(this.active)-->
				<!--this.$router.push('/securitycenter')-->
			<!--},-->
			<!--handleopen() {-->
				<!--//console.log('handleopen');-->
			<!--},-->
			<!--handleclose() {-->
				<!--//console.log('handleclose');-->
			<!--},-->
			<!--selectTop(a, b){-->
				<!--//console.log(a)-->
				<!--//console.log(b)-->
				<!--console.log(this.active)-->
				<!--this.active=a-->
				<!--this.$router.push(a);-->
				<!--console.log(this.active)-->
			<!--},-->
			<!--handleselect: function (a, b) {-->
				<!--console.log(this.active)-->
				<!--//console.log(a)-->
				<!--//console.log(this)-->
				<!--//console.log(this.$router)-->
				<!--var cpath = this.$router.currentRoute.fullPath-->
				<!--//console.log(a)-->
				<!--//console.log(cpath)-->
				<!--var options = this.$router.options.routes-->
				<!--this.active=a-->
				<!--this.$router.push(a);-->
				<!--//console.log(this.$router.options.routes[2].children[0].component)-->
				<!--//this.$router.options.routes[2].children[0].component.methods.getData()-->
				<!--//this.$router.push({ path: '/server' })-->
				<!--//this.$router.push({ path: '/404' })-->
				<!--/*-->
				<!--for(var x in options){-->
					<!--//console.log(options[x])-->
					<!--if(options[x].leaf){-->
						<!--//一个节点-->
						<!--if(options[x].children[0].path==cpath){-->
							<!--//console.log(options[x].component.mounted())-->
							<!--//options[x].component.mounted()-->
							<!--console.log(cpath)-->
						<!--}-->
					<!--}-->
					<!---->
					<!--else{-->
						<!--//多个节点-->
						<!--for(var i in options[x].children){-->
							<!--if(options[x].children[i].path==cpath){-->
								<!--console.log(options[x].children[i].component.mounted())-->
							<!--}else{-->
								<!--for(var j in options[x].children[i].children){-->
									<!--if(options[x].children[i].children[j].path==cpath){-->
										<!--console.log(options[x].children[i].children[j].component.mounted())-->
									<!--}-->
								<!--}-->
							<!--}-->
						<!--}-->
					<!--}-->
					<!--*/-->
				<!--//}-->
				<!---->
			<!--},-->
			<!--//退出登录-->
			<!--logout: function () {-->
				<!--var _this = this;-->
				<!--let user = sessionStorage.getItem('user');-->
				<!--let u = JSON.parse(user);-->
				<!--let logoutParams = { userid: u.userid, token: sessionStorage.getItem("token") }-->
				<!--this.$confirm('确认退出吗?', '提示', {-->
					<!--//type: 'warning'-->
				<!--}).then(() => {-->
					<!--//this.$post(path+"/user/dologout",logoutParams)-->
					<!--sessionStorage.removeItem('user');-->
					<!--sessionStorage.removeItem('token');-->
					<!--_this.$router.push('/login');-->
				<!--}).catch(() => {-->

				<!--});-->
			<!--},-->
			<!--//折叠导航栏-->
			<!--collapse:function(){-->
				<!--this.collapsed=!this.collapsed;-->
				<!--if(this.left==false){-->
					<!--this.left=true;-->
					<!--this.right=false;-->
				<!--}else{-->
					<!--this.left=false;-->
					<!--this.right=true;-->
				<!--}-->

			<!--},-->
			<!--showMenu(i,status){-->
				<!--this.$refs.menuCollapsed.getElementsByClassName('submenu-hook-'+i)[0].style.display=status?'block':'none';-->
			<!--},-->
		<!--},-->
		<!--mounted() {-->
			<!--var vm =this;-->
			<!--var user = sessionStorage.getItem('user');-->
			<!--this.user = user-->
			<!--if (user) {-->
				<!--user = JSON.parse(user);-->
				<!--this.sysUserName = user.nickname || '';-->

				<!--var cpath=this.$router.currentRoute.fullPath;-->
				<!--console.log(cpath)-->
				<!--if(cpath=='/query/queryout'){-->
					<!--this.active='/query/queryin'-->
				<!--}else if(cpath=='/order/orderout'){-->
					<!--this.active='/order/orderin'-->
				<!--}else{-->
					<!--this.active = cpath; -->
				<!--}-->
				<!--if(user.roleid==2){-->
					<!--this.nickname = "厂商平台";-->
					<!--this.platform = true;-->
				<!--}-->
				<!--if(user.roleid==1){-->
					<!--this.nickname = "泊链";-->
					<!--this.bolink = true;-->
				<!--}-->
				<!--if(user.roleid==4){-->
					<!--this.nickname = "车场";-->
					<!--this.park = true;-->
				<!--}-->
				<!--if(user.roleid==3){-->
					<!--this.nickname = "服务商";-->
					<!--this.server = true;-->
				<!--}-->
				<!---->
			<!--}-->
		<!--},-->
		<!--watch:{-->
			<!--ulist:function(val){-->
				<!--this.sysUserName = val.nickname-->
			<!--}-->
		<!--}-->
	<!--}-->

<!--</script>-->
<!--<style lang="scss" scoped>-->
	<!--.container {-->
		<!--position: absolute;-->
		<!--top: 0px;-->
		<!--bottom: 0px;-->
		<!--width: 100%;-->
		<!--.header {-->
			<!--height: 50px;-->
			<!--line-height: 50px;-->
			<!--background: #008F4C;//#324157;//#0080dd;//#35495E;//#1F2D3D-->
			<!--color:#fff;-->
			<!--.userinfo {-->
				<!--text-align: right;-->
				<!--padding-right: 20px;-->
				<!--float: right;-->
				<!--.userinfo-inner {-->
					<!--cursor: pointer;-->
					<!--color:#fff;-->
					<!--img {-->
						<!--width: 40px;-->
						<!--height: 40px;-->
						<!--border-radius: 20px;-->
						<!--margin: 10px 0px 10px 10px;-->
						<!--float: right;-->
					<!--}-->
				<!--}-->
			<!--}-->
			<!--.logo {-->
				<!--//width:180px;-->
				<!--height:50px;-->
				<!--font-size: 22px;-->
				<!--padding-left:20px;-->
				<!--padding-right:20px;-->
				<!--border-color: rgba(238,241,146,0.3);-->
				<!--border-right-width: 1px;-->
				<!--border-right-style: solid;-->
				<!--img {-->
					<!--width: 40px;-->
					<!--float: left;-->
					<!--margin: 10px 10px 10px 18px;-->
				<!--}-->
				<!--.txt {-->
					<!--color:#fff;-->
				<!--}-->
			<!--}-->
			<!--.logo-width{-->
				<!--width:180px;-->
			<!--}-->
			<!--.logo-collapse-width{-->
				<!--width:60px-->
			<!--}-->
			<!--.tools{-->
				<!--padding: 0px 23px;-->
				<!--width:14px;-->
				<!--height: 50px;-->
				<!--line-height: 50px;-->
				<!--cursor: pointer;-->
			<!--}-->
		<!--}-->
		<!--.main {-->
			<!--display: flex;-->
			<!--// background: #324057;-->
			<!--position: absolute;-->
			<!--top: 50px;-->
			<!--bottom: 0px;-->
			<!--overflow: hidden;-->
			<!--aside {-->
				<!--background: #EEF1F6;-->
				<!--flex:0 0 180px;-->
				<!--width: 180px;-->
				<!--// position: absolute;-->
				<!--// top: 0px;-->
				<!--// bottom: 0px;-->
				<!--.el-menu{-->
					<!--height: 100%;-->
				<!--}-->
				<!--.collapsed{-->
					<!--width:60px;-->
					<!--.item{-->
						<!--position: relative;-->
					<!--}-->
					<!--.submenu{-->
						<!--position:absolute;-->
						<!--top:0px;-->
						<!--left:60px;-->
						<!--z-index:99999;-->
						<!--height:auto;-->
						<!--display:none;-->
					<!--}-->

				<!--}-->
			<!--}-->
			<!--.menuitem{-->
				<!--font-size:16px;-->
				<!--margin-left:12px;-->
				<!--//color:black-->
			<!--}-->
			<!--.menu-collapsed{-->
				<!--flex:0 0 60px;-->
				<!--width: 60px;-->
			<!--}-->
			<!--.menu-expanded{-->
				<!--flex:0 0 180px;-->
				<!--width: 180px;-->
			<!--}-->
			<!--.content-container {-->
				<!--// background: #f1f2f7;-->
				<!--flex:1;-->
				<!--// position: absolute;-->
				<!--// right: 0px;-->
				<!--// top: 0px;-->
				<!--// bottom: 0px;-->
				<!--// left: 180px;-->
				<!--overflow-y: hidden;-->
				<!--padding: 10px;-->
				<!--padding-top: 8px;-->
				<!--.breadcrumb-container {-->
					<!--//margin-bottom: 15px;-->
					<!--.title {-->
						<!--width: 180px;-->
						<!--float: left;-->
						<!--color: #475669;-->
					<!--}-->
					<!--.breadcrumb-inner {-->
						<!--float: right;-->
					<!--}-->
				<!--}-->
				<!--.content-wrapper {-->
					<!--background-color: #fff;-->
					<!--box-sizing: border-box;-->
				<!--}-->
			<!--}-->
		<!--}-->
	<!--}-->
<!--</style>-->
