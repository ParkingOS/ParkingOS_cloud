<template>
  <div class="number" style="margin-right: 15px">

		<el-select v-model="selectForm.province" placeholder="省" filterable @change="uptoeditdialog1" style="width:100%">
			<el-option
				v-for="item in provincelist"
				:label="item.name"
				:value="item.proID"
				>
			</el-option>
		</el-select>

		<el-select v-model="selectForm.city" placeholder="市" filterable @change="uptoeditdialog2"  style="width:100%;margin-top:15px">
			<el-option
				v-for="item in citylist"
				:label="item.name"
				:value="item.cityID"
				>
			</el-option>
		</el-select>

		<el-select v-model="selectForm.distincts" placeholder="县/区" filterable @change="uptoeditdialog3" style="width:100%;margin-top:15px">
			<el-option
				v-for="item in distinctslist"
				:label="item.disName"
				:value="item.id"
				>
			</el-option>
		</el-select>

  </div>
</template>

<script>
import common from '../../common/js/common.js'
import { path } from '../../api/api';

export default {
  name: 'select',
  data () {
    return {
			provincelist:'',
			citylist:'',
			distinctslist:'',
			selectForm:{
				province:'',
				city:'',
				distincts:'',
			},
			tempForm:{
				province:'',
				city:'',
				distincts:'',
			},
			upForm:{
				province:'',
				city:'',
				distincts:'',
			}
    }
  },
  props:['rowdata','disable','hasDistincts'],
  methods:{
		uptoeditdialog1:function(){
			if(this.selectForm.province==''){
				this.citylist=''
				return
			}
			var _this = this
			//1清空23
			this.selectForm.city=''
			this.selectForm.distincts=''
			//2上传数据
			this.upForm.province=this.selectForm.province
			//this.$emit('fromedititem',this.upForm)
			//3查询下一级列表
			if(this.selectForm.province>0){
				this.$post(path+'/getdata/getcitylist',{ProID:this.selectForm.province},function(ret){
					_this.citylist = ret
				})
			}

		},
		uptoeditdialog2:function(){
			if(this.selectForm.city==''){
				this.distinctslist=''
				return
			}
			var _this = this
			//1清空23
			this.selectForm.distincts=''
			//2上传数据
			this.upForm.city=this.selectForm.city
			//3查询下一级列表
			if(this.selectForm.city>0){
				this.$post(path+'/getdata/getdistinctlist',{CityID:this.selectForm.city},function(ret){
					if(ret.length<1){
						_this.distinctslist = ''
						_this.selectForm.distincts=null
					}else{
						_this.distinctslist = ret
					}
					_this.$emit('fromedititem',_this.upForm)
				})
			}


		},
		uptoeditdialog3:function(){
			this.upForm.distincts=this.selectForm.distincts
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
			this.selectForm=common.clone(this.tempForm)
			this.upForm={}
		}
  	},
	mounted(){
		var _this = this
		this.$post(path+'/getdata/getprovincelist',function(ret){
				_this.provincelist = ret
		})
	},
}
</script>