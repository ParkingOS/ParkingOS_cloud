<template>
  <div class="number" style="margin-right: 15px">
				<el-select v-model="selectForm.select" filterable @change="uptoeditdialog" :disabled="disable" style="width:100%">
					<el-option
						v-for="item in selectlist"
						:label="item.value_name"
						:value="item.value_no"
						>
					</el-option>
				</el-select>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'select',
  data () {
    return {
			selectForm:{
				select:''
			},
			tempForm:{
				select:''
			},
			upForm:{}
    }
  },
	props:['selectlist','id','rowdata','disable'],
  methods:{
		uptoeditdialog:function(){
			if(this.select!=''){
				this.upForm[this.id]=this.selectForm.select
				this.$emit('fromedititem',this.upForm)
			}
		},
		setValue:function(){
				this.selectForm=common.clone(this.tempForm)
				this.upForm={}
		}
  },
}
</script>