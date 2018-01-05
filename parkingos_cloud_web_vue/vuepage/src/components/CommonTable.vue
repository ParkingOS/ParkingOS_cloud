<template>
	<section>
		<!--工具条-->
		<el-row style="margin-bottom:8px" v-if="!hideTool">
			<el-col :span="24" v-if="!showRight" align="left">
					<el-col :span="18" align="left">

						<div v-if="showCenterInfo" style="display:inline;margin-right:400px;float: left">
						 <el-input v-model="todayTotal" style="width:150px;background:white" disabled>
							<template slot="prepend">今日收钞</template>
						 </el-input>&nbsp;&nbsp;&nbsp;
						 <el-input v-model="balance" style="width:150px;background:white" disabled>
							<template slot="prepend">钱箱余额</template>
						</el-input>
							&nbsp;&nbsp;收款人:<el-select v-model="centralpayment" @change="getCentralPaymentMoney" placeholder="全部" style="width:150px;margin-left:20px">
							<el-option
							v-for="item in centralpaymentlist"
							:label="item.value_name"
							:value="item.value_no">
							</el-option>
						</el-select>
						</div>
						<el-button type="primary"  size="small" @click="handleSearch" v-if="!hideSearch" icon="search">高级查询</el-button>
					<el-tooltip class="item" effect="dark" content="导出内容为当前查询条件下所有数据" placement="bottom">
						<el-button type="primary" size="small" @click="handleExport" v-if="!hideExport">导出</el-button>
					</el-tooltip>
						<el-button type="primary" size="small" @click="handleAdd" v-if="!hideAdd">{{addtitle}}</el-button>

						<div v-if="showParkInfo"  style="float: left;width: 370px;">
						 <el-input v-model="totalCount" style="width:135px;background:white" disabled>
							<template slot="prepend">交易笔数</template>
						 </el-input>&nbsp;&nbsp;&nbsp;
						 <el-input v-model="money" style="width:170px;background:white" disabled>
							<template slot="prepend">交易额</template>
						 </el-input>
						</div>
						<div v-if="showdateSelector" style="float: left;">
							<span class="demonstration">日期</span>
							<el-date-picker
									v-model="datesselector"
									type="datetimerange"
									align="right"
									unlink-panels
									range-separator="至"
									start-placeholder="开始日期"
									end-placeholder="结束日期"
									:picker-options="pickerOptions2"
									@change="changeanalysisdate">
							</el-date-picker>
						</div>
						<!--统计页面日期选框-->
						<div v-if="showanalysisdate">
							<el-date-picker
								v-model="analysisdate"
								type="date"
								placeholder="选择日期"
								:picker-options="analysisdateopt"
								@change="changeanalysisdate">
							</el-date-picker>
						</div>

					</el-col>

					<el-col :span="6" align="right" >
						<!--<span style="color:red;font-size:8px">提示:刷新后会重置高级查询</span>-->
						<!--<el-button @click="reset" type="primary" size="small">清空高级查询</el-button>-->
						<el-button @click="refresh" type="text" size="small">刷新&nbsp;&nbsp;</el-button>
					</el-col>
			</el-col>
			<el-col :span="24" v-if="showRight">
					<el-col :span="4" align="left" style="height:36px;padding-top:8px">
						<span style="font-size:15px;font-weight:bold" v-if="showLeftTitle">{{leftTitle}}</span>
					</el-col>
					<el-col :span="20" align="right">
						<el-button type="primary" @click="handleSearch" size="small" v-if="!hideSearch">高级查询</el-button>
						<el-tooltip class="item" effect="dark" content="导出内容为当前查询条件下所有数据" placement="bottom">
							<el-button type="primary" @click="handleExport" size="small" v-if="!hideExport">导出</el-button>
						</el-tooltip>
						<el-button type="primary" @click="handleAdd" size="small" v-if="!hideAdd">{{addtitle}}</el-button>
						<el-button @click="refresh" type="text" size="small">刷新</el-button>
					</el-col>
			</el-col>
		</el-row>
		<!--列表-->
		<el-table :data="table" border highlight-current-row style="width:100%;" :height="tableheight" v-loading="loading"  @sort-change="sortChange">
				 <el-table-column
					align="center"
					type="index"
					width="83"
					label=" "
					fixed="left"
					>
				</el-table-column>

				<el-table-column label="操作" :width="btswidth" v-if="!hideOptions" align="center" fixed="left" >
					<template scope="scope">
						<el-button size="small" type="text" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
						<el-button v-if="showresetpwd" size="small" type="text" @click="handleresetpwd(scope.$index, scope.row)" ><span style="color:#008F4C">重置密码</span></el-button>
						<el-button v-if="showsetting" size="small" type="text" @click="handlesetting(scope.$index, scope.row)" >设置</el-button>
						<el-button v-if="showqrurl" size="small" type="text" @click="handleqrurl(scope.$index, scope.row)" >生成车场二维码</el-button>
						<el-button v-if="showdelete" size="small" type="text" @click="openDelete(scope.$index, scope.row)" ><span style="color:red">删除</span></el-button>
						<el-button v-if="showmapdialog" size="small" type="text" @click="handlemap(scope.$index, scope.row)" ><span style="color:#008F4C">车场定位</span></el-button>
						<!--
						<el-button
							v-for="bt in bts"
							v-on:click="bt.func"
						>{{bt.name}}</el-button>
						-->
					</template>
				</el-table-column>

				<div v-for="items in tableitems">
					<div v-if="items.hasSubs">
						<el-table-column
							:label="items.label"
							header-align="center"
						>
							<el-table-column
								v-for="tableitem in items.subs"
								v-if="!tableitem.hidden"
								:prop="tableitem.prop"
								:label="tableitem.label"
								header-align="center"
								:align="tableitem.align"
								:sortable="!tableitem.unsortable"
								:width="tableitem.width"
								:formatter="tableitem.format"
								>
							</el-table-column>
						</el-table-column>
					</div>
					<div v-if="!items.hasSubs">
						<el-table-column
								v-for="tableitem in items.subs"
								v-if="!tableitem.hidden"
								:prop="tableitem.prop"
								:label="tableitem.label"
								header-align="center"
								:align="tableitem.align"
								:sortable="!tableitem.unsortable"
								:width="tableitem.width"
								:formatter="tableitem.format"
								>
						</el-table-column>
					</div>

				</div>

		  </el-table>

		<!--工具条-->
		<el-col :span="24" v-if="!hidePagination" align="bottom" style="margin-top:5px;margin-bottom:5px">
			<el-col :span="24" align="right">
			<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[20, 40, 80]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
			</el-col>
		</el-col>

		<!--高级查询-->
		<complex-search
			:searchVisible="searchFormVisible"
			:title="searchtitle"
			:searchitems="tableitems"
			v-on:searchdialog="closesearch"
			v-on:search="onSearch"
			ref="search">
		</complex-search>

		<!--表格编辑-->
		<edit-form
			:editVisible="editFormVisible"
			:edititems="tableitems"
			:editloading="editloading"
			:editFormRules="editFormRules"
			:rowdata="rowdata"
			:dialogsize="dialogsize"
			v-on:editdialog="closeedit"
			v-on:edit="onEdit"
			:ref="ef">
		</edit-form>

		<!--表格添加-->
		<add-form
			:addtitle="addtitle"
			:addVisible="addFormVisible"
			:addloading="addloading"
			:additems="tableitems"
			:addFormRules="typeof(addFormRules)=='undefined'?editFormRules:addFormRules"
			:dialogsize="dialogsize"
			v-on:adddialog="closeadd"
			v-on:add="onAdd"
			:ref="af">
		</add-form>

		<!--删除提示框-->
		<el-dialog
			title="提示"
			v-model="delVisible"
			size="tiny"
			custom-class="deleteTip">
			<div class="el-message-box__status el-icon-warning"></div><br/>
			<div style="margin-left:50px;vertical-align:middle;">确定删除吗?此操作不可恢复!</div>
			<span slot="footer" class="dialog-footer">
				<el-button @click="delVisible = false" size="small">取 消</el-button>
				<el-button type="primary" @click="handledelete" size="small">确 定</el-button>
			</span>
		</el-dialog>

		<!--地图-->
 		<input v-show="false" v-model.number="center.lng">
   		<input v-show="false" v-model.number="center.lat">

		<el-dialog v-model="mapVisible" @close="dclose" top="10%">
			<div>
			<baidu-map v-if="showMap" :style="mapstyle" :center="center" :zoom="16" @click="clickmap" @dblclick="makePoint" :scroll-wheel-zoom="true" :double-click-zoom="false">

				<bm-marker v-if="showMarker" :position="marker" animation="BMAP_ANIMATION_DROP" :label="label" :dragging="true" @mouseup="mouseup"></bm-marker>
				<!--<bm-local-search :keyword="keyword" :auto-viewport="true" :selectFirstResult="true" :pageCapacity="ps" :resultPanel="false" location="北京"></bm-local-search>-->
 	   		</baidu-map>
			</div>
			<el-col :span="24"  style="margin-bottom: 10px;margin-top:10px">
				<el-col :span="16">
				<el-input
					placeholder="请输入关键字"
					v-model="keyword"
					style="width:150px;"
					size="small">
				</el-input>
				<el-button type="primary" icon="search" size="small" @click="sclick"></el-button>
				</el-col>
				<el-col :span="8" align="right">
				<el-button @click="mapVisible = false" size="small">取 消</el-button>
				<el-button type="primary" @click="modifyPosition" size="small" :loading="maploading">保存</el-button>
				</el-col>
			</el-col>
		</el-dialog>

		<!--重置缴费机密码-->
		<el-dialog
			title="重置密码"
			v-model="resetPwdVisible"
			size="tiny">
			<el-form ref="form"  label-width="120px" style="margin-bottom:-30px">
			<el-form-item label="请输入新密码">
				<el-input v-model="pwd1" style="width:90%"></el-input>
			</el-form-item>
			<el-form-item label="再次输入密码">
				<el-input v-model="pwd2" style="width:90%"></el-input>
			</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer" >
				<el-button @click="resetPwdVisible = false" size="small">取 消</el-button>
				<el-button type="primary" size="small" @click="resetPwd" :loading="resetloading">确 定</el-button>
			</span>
		</el-dialog>

	</section>
</template>

<script>
	import { path } from '../api/api';
	import common from '../common/js/common'
	import ComplexSearch from './ComplexSearch'
	import EditForm from './EditForm'
	import AddForm from './AddForm'
	import axios from 'axios'

	export default {
		components:{
			ComplexSearch,EditForm,AddForm
		},
		data() {
			return {
				ef:'editref',
				af:'addref',
				searchFormVisible:false,
				editFormVisible:false,
				addFormVisible:false,
				mapVisible:false,
				delVisible:false,
				currentPage: 1,
				pageSize: 20,
				total:0,
				orderby:'desc',
				orderfield:'id',
				table:[],
				loading: false,
				resetloading:false,
				editloading:false,
				addloading:false,
				showMarker:false,
				//showMap:false,
				maploading:false,
				centralpaymentlist:'',
				searchForm:{},
				tempSearchForm:{},
				sform:{},
				rowdata:{},

				center:{
					lat:0,
					lng:0
				},
				marker:{
					lat:0,
					lng:0
				},
				mapstyle:'',
				mapheight:'',
				mapwidth:'',
				rowid:0,
				ps:1,
				keyword:'',
				cityName:'',
				money:'0.00 元',
				totalCount:'0',
				label:{content: 'Marker Label', opts: {offset: {width: 20, height: -10}}},
				centralpayment:-3,
				todayTotal:'',
				balance:'',
				analysisdate:'',
                datesselector:'',
				searchDate:'',
				analysisdateopt:{
					disabledDate(time) {
						return time.getTime() > Date.now();
					}
				},
                pickerOptions2: {
                    shortcuts: [
                        {
                            text: '今天',
                            onClick(picker) {
                                const end = new Date();
                                const start = new Date();
                                start.setHours(0);
                                start.setMinutes(0);
                                start.setSeconds(0);
                                end.setHours(23);
                                end.setMinutes(59);
                                end.setSeconds(59);
                                //start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                                picker.$emit('pick', [start, end]);
                                this.sform.page=1;
                            }
                        },
                        {
                        text: '最近一周',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                            start.setHours(0);
                            start.setMinutes(0);
                            start.setSeconds(0);
                            end.setHours(23);
                            end.setMinutes(59);
                            end.setSeconds(59);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近一个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                            start.setHours(0);
                            start.setMinutes(0);
                            start.setSeconds(0);
                            end.setHours(23);
                            end.setMinutes(59);
                            end.setSeconds(59);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近三个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                            start.setHours(0);
                            start.setMinutes(0);
                            start.setSeconds(0);
                            end.setHours(23);
                            end.setMinutes(59);
                            end.setSeconds(59);
                            picker.$emit('pick', [start, end]);
                        }
                    }]
                },
				resetPwdVisible:false,
				pwd1:'',
				pwd2:'',
			}
		},
		props:['tableitems','fieldsstr','hideOptions','hideExport','hideAdd','hideSearch', 'showRight','showLeftTitle','leftTitle', 'editFormRules','addFormRules',
			'tableheight','bts','btswidth','queryapi','queryparams', 'exportapi','editapi','addapi','delapi', 'searchtitle','addtitle','addfailmsg',
			'dialogsize','showqrurl','showdelete','showmapdialog','showMap','showsetting','hidePagination','showParkInfo','hideTool','showCenterInfo','showanalysisdate','showresetpwd','showdateSelector'],
		methods: {
			//控制表格样式
			rowstyle(row,index){
				if(index==0){
					return 'indextext'
				}
			},
			//刷新页面
			refresh(){
				console.log('refresh')
				if(this.showCenterInfo){
					this.getCentralPaymentMoney()
				}else if(this.showdateSelector){
	
                    //this.$extend(this.sform,{'date':this.datesselector})
					this.sform.date= this.searchDate
					this.getTableData(this.sform);
				}else{
                    this.getTableData(this.sform);
                }
				//清空高级查询表单项内容
				this.$message({
					message: '刷新成功!',
					type: 'success',
					duration: 600
				});
			},
			//重置高级查询
			reset(){
				console.log('reset')
				this.getTableData({});
				//清空高级查询表单项内容
				this.$refs['search'].resetSearch()
				this.sform = common.clone(this.tempSearchForm)
				this.$message({
					message: '清空成功!',
					type: 'success',
					duration: 600
				});
			},
			//分页变动
			handleSizeChange(val) {
				this.pageSize=val;
				console.log('size change')
				this.getTableData(this.sform);
			},
			handleCurrentChange(val) {
				this.currentPage = val;
				console.log('page change')
                this.sform.date= this.searchDate
				this.getTableData(this.sform);
			},
			//排序变动
			sortChange(val){
				if(val.order!=null&&val.order.substring(0,1)=="a"){
					this.orderby = "asc";
				}else{
					this.orderby = "desc";
				}
				this.orderfield=val.prop;
				console.log('sort change')
				this.getTableData(this.sform);
			},
			//拉取表格数据
			getTableData(sform){
				console.log('getdata')
				var vm=this;
				this.loading = true;
				var api=this.queryapi;
				//alert(sform);
				this.$extend(sform,{'rp':this.pageSize})
				this.$extend(sform,{'page':this.currentPage})
				this.$extend(sform,{'orderby':this.orderby})
				this.$extend(sform,{'orderfield':this.orderfield})
				this.$extend(sform,{'fieldsstr':this.fieldsstr})
				this.$extend(sform,this.queryparams)
				this.$extend(sform,{'token':sessionStorage.getItem('token')})
				vm.$post(path+api,sform,function(ret){
					if(ret.validate!='undefined'&&ret.validate=='0'){
						vm.loading = false;
						//未携带令牌.重新登录
						setTimeout(()=>{vm.alertInfo('未携带令牌,请重新登录!')},150)
					}else if(ret.validate!='undefined'&&ret.validate=='1'){
						vm.loading = false;
						//过期.重新登录
						setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},150)
					}else if(ret.validate!='undefined'&&ret.validate=='2'){
						vm.loading = false;
						//令牌无效.重新登录
						setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},150)
					}else{
						if(ret.total==0){
							vm.table=[];
                            vm.money='0元';
                            vm.totalCount=0;
						}else{
							vm.table=ret.rows;
							vm.money=ret.money+'元';
							vm.totalCount=ret.totalCount;
						}
						vm.total=ret.total;
						vm.loading = false;
					}

				},"json");
			},
            //拉取表格数据
            getTableDataCloud(sform){
                console.log('getdata')
                var vm=this;
                this.loading = true;
                var api=this.queryapi;
                //alert(sform);
                this.$extend(sform,{'rp':this.pageSize})
                this.$extend(sform,{'page':this.currentPage})
                this.$extend(sform,{'orderby':this.orderby})
                this.$extend(sform,{'orderfield':this.orderfield})
                this.$extend(sform,{'fieldsstr':this.fieldsstr})
                this.$extend(sform,this.queryparams)
                this.$extend(sform,{'token':sessionStorage.getItem('token')})
                vm.$post(api,sform,function(ret){
                    if(ret.validate!='undefined'&&ret.validate=='0'){
                        vm.loading = false;
                        //未携带令牌.重新登录
                        setTimeout(()=>{vm.alertInfo('未携带令牌,请重新登录!')},150)
                    }else if(ret.validate!='undefined'&&ret.validate=='1'){
                        vm.loading = false;
                        //过期.重新登录
                        setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},150)
                    }else if(ret.validate!='undefined'&&ret.validate=='2'){
                        vm.loading = false;
                        //令牌无效.重新登录
                        setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},150)
                    }else{
                        if(ret.total==0){
                            vm.table=[];
                            vm.money='0元';
                            vm.totalCount=0;
                        }else{
                            vm.table=ret.rows;
                            vm.money=ret.money+'元';
                            vm.totalCount=ret.totalCount;
                        }
                        vm.total=ret.total;
                        vm.loading = false;
                    }

                },"json");
            },
			//高级查询
			handleSearch(){
				//弹出高级查询界面
				//全平台服务商
				var vm = this
				var user = sessionStorage.getItem('user')
				user = JSON.parse(user)
				for(var i=0;i<this.tableitems.length;i++){
					if(this.tableitems[i].customSelect=='parkserver'){
						//重置该selectlist,根据
						var params;
						if(user.roleid==1){
							if(this.tableitems[i].searchSelect=='all'){
								params = {'query':1,'token':sessionStorage.getItem('token')}
							}
						}else if(user.roleid==2){
							if(this.tableitems[i].searchSelect=='local_all'){
								params = {'token':sessionStorage.getItem('token')}
							}else if(this.tableitems[i].searchSelect=='all'){
								params = {'query':1,'token':sessionStorage.getItem('token')}
							}
						}
						this.$ajax({
							url:path+'/getdata/serverlist',
							data:params,
							async: false,
							success:function(ret){
								vm.tableitems[i].selectlist=ret
							}
						})
					}else if(this.tableitems[i].customSelect=='park'){
						var params;
						var params;
						if(user.roleid==1){
						}else if(user.roleid==2){
							if(this.tableitems[i].searchSelect=='local_all'){
								params = {'token':sessionStorage.getItem('token')}
							}else if(this.tableitems[i].searchSelect=='all'){
								params = {'query':1,'token':sessionStorage.getItem('token')}
							}
						}else if(user.roleid==3){
							if(this.tableitems[i].searchSelect=='local_all'){
								params = {'token':sessionStorage.getItem('token')}
							}
						}
						this.$ajax({
							url:path+'/getdata/parklist',
							data:params,
							async: false,
							success:function(ret){
								vm.tableitems[i].selectlist=ret
							}
						})
					}
				}
				this.searchFormVisible = true;
			},
			closesearch:function(val){
				this.searchFormVisible=val;
			},
			onSearch:function(sform){
				//在这里得到表单项,提交查询
				this.sform=sform
				this.getTableData(sform)
			},
			//表格编辑
			handleEdit(index, row){
				//拿到当前行数据row,传递给表单编辑子组件,子组建中包括重置和保存按钮
				this.rowdata = row;
				var vm = this
				var user = sessionStorage.getItem('user')
				user = JSON.parse(user)
				for(var i=0;i<this.tableitems.length;i++){
					if(this.tableitems[i].customSelect=='parkserver'){
						//重置该selectlist,根据
						var params;
						if(user.roleid==1){
							if(this.tableitems[i].commonSelect=='local_all'){
								params = {'com_id':row.id,'state':1,'token':sessionStorage.getItem('token')}
							}else if(this.tableitems[i].commonSelect=='all'){
								params = {'query':1,'token':sessionStorage.getItem('token')}
							}
						}else if(user.roleid==2){
							if(this.tableitems[i].commonSelect=='local_available'){
								params = {'state':1,'token':sessionStorage.getItem('token')}
							}
						}
						this.$ajax({
							url:path+'/getdata/serverlist',
							data:params,
							async: false,
							success:function(ret){
								vm.tableitems[i].selectlist=ret
							}
						})
					}
				}
				//获取角色编号,获取rowid,
				this.editFormVisible = true;
			},
			//
			handleqrurl(index, row){
				//调用父组件的方法,传row
				this.$emit('qrurl',row.park_id)
			},
			//单击设置触发
			handlesetting(index, row){
				//调用父组件的方法,传row
				this.$emit('setting',row.id)
			},
			//导出表格数据
			handleExport(){
				var vm = this;
				var api = this.exportapi;
				var params = ''
				if(common.getLength(this.sform)==0){
				 	params = 'fieldsstr='+this.fieldsstr+'&token='+sessionStorage.getItem('token')
				}else{
					for(var x in this.sform){
						//console.log(this.sform[x])
						params += x+'='+this.sform[x]+'&'
					}
				}
				console.log(params)
				window.open(path+api + '?'+params);
				//window.location.href(path+api + '?fieldsstr='+this.fieldsstr)
				//this.$.get(path+api,params)
			},
			closeedit:function(val){
				this.editFormVisible=val
				this.editloading=val
			},
			onEdit:function(eform){
				//发送ajax,提交表单更新
				var vm = this;
				var api=this.editapi;
				var qform = this.sform;
				this.$extend(eform,{'token':sessionStorage.getItem('token')})
				this.$refs.editref.$refs.editForm.validate((valid) => {
					if (valid) {
						vm.editloading=true;
						vm.$post(path+api,eform,function(ret){
							if(ret.validate!='undefined'&&ret.validate=='1'){
								//过期.重新登录
								setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},100)
							}else if(ret.validate!='undefined'&&ret.validate=='2'){
								//令牌无效.重新登录
								setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},100)
							}else{
								if(ret>0){
								//更新成功
								vm.getTableData(qform);
								vm.$message({
									message: '更新成功!',
									type: 'success',
									duration: 600
								});
								vm.editFormVisible = false;
								}else{
									//更新失败
									vm.$message({
										message: '更新失败!',
										type: 'error',
										duration: 600
									});
								}
								setTimeout('vm.editloading=false',5000)
							}

						},"json")
					}
				});
			},
			handleAdd(){
				var vm = this
				var user = sessionStorage.getItem('user')
				user = JSON.parse(user)
				for(var i=0;i<this.tableitems.length;i++){
					if(this.tableitems[i].customSelect=='parkserver'){
						//重置该selectlist,根据
						var params;
						if(user.roleid==1){
							if(this.tableitems[i].commonSelect=='local_all'){
								params = {'com_id':row.id,'state':1,'token':sessionStorage.getItem('token')}
							}else if(this.tableitems[i].commonSelect=='all'){
								params = {'query':1,'token':sessionStorage.getItem('token')}
							}
						}else if(user.roleid==2){
							if(this.tableitems[i].commonSelect=='local_available'){
								params = {'state':1,'token':sessionStorage.getItem('token')}
							}
						}
						this.$ajax({
							url:path+'/getdata/serverlist',
							data:params,
							async: false,
							success:function(ret){
								vm.tableitems[i].selectlist=ret
							}
						})
					}
				}
				this.addFormVisible=true
			},
			closeadd(val){
				this.addFormVisible=val
				this.addLoading=val;
			},
			onAdd(aform){
				console.log(aform)
				//发送请求,添加一条记录
				var vm = this;
				var api=this.addapi;
				var qform = this.sform;
				var msg = this.addfailmsg;
				this.$extend(aform,{'token':sessionStorage.getItem('token')})
				this.$refs.addref.$refs.addForm.validate((valid) => {
					if (valid) {
						vm.addloading=true
						vm.$post(path+api,aform,function(ret){
							if(ret.validate!='undefined'&&ret.validate=='1'){
								//过期.重新登录
								setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},100)
							}else if(ret.validate!='undefined'&&ret.validate=='2'){
								//令牌无效.重新登录
								setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},100)
							}else{
								if(ret>0){
								//更新成功
								vm.getTableData(qform);
								vm.$message({
									message: '添加成功!',
									type: 'success',
									duration: 600
								});
								vm.addFormVisible = false;
								vm.addloading=false
								}else{
									//更新失败
									vm.$message({
										message: msg,
										type: 'error',
										duration: 1200
									});
								}
							}

						},"json")
					}
				});
			},
			openDelete(index,row){
				this.rowid=row.id
				this.delVisible=true
			},
			//删除
			handledelete(){
				var vm = this;
				var api=this.delapi;
				var qform = this.sform;
				var dform = {'id':this.rowid,'token':sessionStorage.getItem('token')}
				//发送请求,删除id为row.id的数据
				vm.$post(path+api,dform,function(ret){
					if(ret.validate!='undefined'&&ret.validate=='1'){
						//过期.重新登录
						setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},100)
					}else if(ret.validate!='undefined'&&ret.validate=='2'){
						//令牌无效.重新登录
						setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},100)
					}else{
						if(ret>0){
							//删除成功
							vm.getTableData(qform);
							vm.$message({
								message: '删除成功!',
								type: 'success',
								duration: 600
							});
							vm.delVisible=false
						}else{
							//更新失败
							vm.$message({
								message: "更新失败",
								type: 'error',
								duration: 1200
							});
						}
					}

				},"json")
			},
			alertInfo(msg){
				this.$alert(msg, '提示', {
					confirmButtonText: '确定',
					type: 'warning',
					callback: action => {
						sessionStorage.removeItem('user');
						sessionStorage.removeItem('token');
						this.$router.push('/login');
					}
				});
			},
			handlemap(index, row){

			    if(row.lat==null ||row.lat=='null' ){
			        row.lat =39.915797;
			        row.lng = 116.404119;
				}
				this.rowid=row.id
				this.label.content=row.name
				this.center.lat=row.lat
				this.center.lng=row.lng
				this.marker.lat=row.lat
				this.marker.lng=row.lng
				this.showMap=true
				this.showMarker=true
				this.mapVisible=true
				console.log(this.center.lat,this.center.lng)
			},
			handleresetpwd(index,row){
				this.rowid=row.id
				this.pwd1=''
				this.pwd2=''
				//显示充值密码对话框
				this.resetPwdVisible=true
			},
			resetPwd(){

				var qform = this.sform;
				var vm = this
				if(this.pwd1==''||this.pwd2==''){
					this.$message.error('密码不能为空!');
					return;
				}
				if(!(/^(\w){6,12}$/.test(this.pwd1))||!(/^(\w){6,12}$/.test(this.pwd2))){
					this.$message.error('密码为6-12位字母,数字或下划线!');
					return
				}
				if(this.pwd1!=this.pwd2){
					this.$message.error('两次输入密码不一致!');
					return
				}
				this.resetloading=true
				vm.$post(path+'/centralpaymentweb/resetpwd',{'pwd':this.pwd1,'id':this.rowid,'token':sessionStorage.getItem('token')},function(ret){
					if(ret.validate!='undefined'&&ret.validate=='1'){
						//过期.重新登录
						setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},100)
					}else if(ret.validate!='undefined'&&ret.validate=='2'){
						//令牌无效.重新登录
						setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},100)
					}else{
						if(ret>0){
							//更新成功
							vm.getTableData(qform);
							vm.$message({
								message: '重置成功!',
								type: 'success',
								duration: 1500
							});
							vm.resetPwdVisible=false
							vm.resetloading=false
						}else{
							//更新失败
							vm.$message({
								message: '更新失败!',
								type: 'error',
								duration: 2000
							});
						}
					}
				},"json")

			},
			modifyPosition(){
				var vm = this;
				var api=this.editapi;
				var eform = {'id':this.rowid,'lng':this.marker.lng,'lat':this.marker.lat,'token':sessionStorage.getItem('token')}
				var qform = this.sform;
				//发起修改位置
				vm.maploading=true;
				vm.$post(path+api,eform,function(ret){
					if(ret.validate!='undefined'&&ret.validate=='1'){
						//过期.重新登录
						setTimeout(()=>{vm.alertInfo('登录过期,请重新登录!')},100)
					}else if(ret.validate!='undefined'&&ret.validate=='2'){
						//令牌无效.重新登录
						setTimeout(()=>{vm.alertInfo('登录异常,请重新登录!')},100)
					}else{
						if(ret>0){
						//更新成功
						vm.getTableData(qform);
						vm.$message({
							message: '更新成功!',
							type: 'success',
							duration: 600
						});
						vm.mapVisible = false;
						vm.maploading=false;
						}else{
							//更新失败
							vm.$message({
								message: '更新失败!',
								type: 'error',
								duration: 600
							});
						}
					}
				},"json")
			},
			makePoint(type){
				this.showMarker=false
				this.marker.lat=type.point.lat
				this.marker.lng=type.point.lng
				this.showMarker=true
				//console.log(this.marker.lat,this.marker.lng)
			},
			mouseup(type){
				this.marker.lat=type.point.lat
				this.marker.lng=type.point.lng
				//console.log(this.marker.lat,this.marker.lng)
			},
			clickmap(type){
				//console.log(type.point)
			},
			sclick(){
				var vm = this
				var myGeo = new BMap.Geocoder();

				myGeo.getPoint(this.keyword, function(point){
					if (point) {
					    if(point.lat==vm.center.lat&&point.lng==vm.center.lng){
                            alert("输入的地址相同或地址不正确!");
						}else{
                            vm.center.lat=point.lat
                            vm.center.lng=point.lng
                            vm.showMarker=false
                            vm.label.content=vm.keyword
                            vm.marker.lat=point.lat
                            vm.marker.lng=point.lng
                            vm.showMarker=true
						}
					}else{
						alert("您选择地址没有解析到结果!");
					}
				}, "中国");
			},
			dclose(){
				console.log('close')
				setTimeout(()=>{this.showMarker=false;this.showMap=false;this.keyword=''},100)
			},
			mapready(map){
				alert("map render")
			},
			mapSearch(){

			},
			getCentralPaymentMoney(){
				var _this = this
				this.$extend(this.sform,{machine_id:this.centralpayment,machine_id_start:this.centralpayment})
				this.getTableData(this.sform)
				this.$post(path+'/centralpaymentweb/getcentralpaymentmoney',{machine_id:this.centralpayment,token:sessionStorage.getItem('token')},function(ret){
					_this.balance = ret.balance +' 元'
					_this.todayTotal = ret.today_total + ' 元'
				})
			},
			changeanalysisdate(input){
				//修改车场统计分析日期
				console.log(input)
				var date = {'date':input}
				this.searchDate = input;
                this.currentPage=1;
				this.getTableData(date)
			}
		},
		mounted() {
			//window.onresize=()=>{alert('123');this.mapheight=common.gwh()*0.5}
			this.mapheight=common.gwh()*0.5
			this.mapstyle='width:inherit;height:'+420+'px'
			console.log('commontable mount')
			//拷贝查询表单,用来在重置时清空表单内容
			this.tempSearchForm=common.clone(this.searchForm)
		},
		activated(){
			//window.onresize=()=>{alert('123');this.mapheight=common.gwh()*0.5}
			var _this=this
			if(this.showCenterInfo){
				//发送请求
				axios.all([common.getCentralPaymentList()])
					.then(axios.spread(function(union,server,park){
						_this.centralpaymentlist=union.data
						_this.centralpayment='-3'
				}))
				this.getCentralPaymentMoney()
			}
			this.analysisdate = Date.now()
			this.mapheight=common.gwh()*0.5
			this.mapstyle='width:inherit;height:'+420+'px'
			console.log('commontable active')
			this.currentPage = 1
			this.sform={}
			//this.date_selector ='123434342'
		},
	}

</script>

<style>
.deleteTip{
	vertical-align:middle
}
.el-table__fixed{
	box-shadow:0 0 0 #fff;
}
.el-input.is-disabled .el-input__inner {
    background-color: #fff;
	color:black
}
.el-input-group>.el-input__inner {
    text-align: center;
}
</style>
