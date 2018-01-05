<template>
  <div class="number" style="margin-right: 15px">
  	<el-col :span="24" style="margin-bottom:5px">
			<el-col :span="6" style="text-align:right;font-size:15px;padding-top:7px;padding-right:8px">
				{{title}}:
			</el-col>
			<el-col :span="5" style="text-align:right;font-size:15px;padding-top:7px">
				等于:
			</el-col>

			<el-col :span="13" align="right" > 
				<el-select v-model="selectForm.select_start" filterable @change="uptosearchdialog" style="width:95%" size="small">
					<el-option
						v-for="item in selectlist"
						:label="item.value_name"
						:value="item.value_no">
					</el-option>
				</el-select>
			</el-col>
		</el-col>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'select',
  data () {
    return {
			selectForm:{
				select_start:''
			},
			tempForm:{
				select_start:''
			},
			upForm:{}
    }
  },
	props:['title','id','selectlist'],
  methods:{
		uptosearchdialog:function(){
			if(this.select_start!=''){
				this.upForm[this.id+'_start']=this.selectForm.select_start
				this.upForm[this.id]=this.selectForm.select_start
				this.$emit('fromsearchitem',this.upForm)
			}
		},
		cleanf:function(val){
			this.upForm={}
			this.selectForm=common.clone(this.tempForm)
		},
  },
	mounted(){
		console.log('mount select')
	},
	destroyed(){
		console.log('destroy select')
	}

}
</script>