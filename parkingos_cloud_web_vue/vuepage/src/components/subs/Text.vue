<template>
  <div class="number" style="margin-right: 15px">
    <el-col :span="24" style="margin-bottom:5px">
			<el-col :span="6" style="text-align:right;font-size:15px;padding-top:7px;padding-right:8px">
				{{title}}:
			</el-col>
			<el-col :span="5" style="text-align:right;font-size:15px;padding-top:7px">
				包含:
			</el-col>

			<el-col :span="13" align="right" > 
				<el-input v-model="textForm.text" style="width:95%" @blur="uptosearchdialog" size="small"></el-input>
			</el-col>
		</el-col>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'text',
  data () {
    return {
			textForm:{
				text:''
			},
			tempForm:{
				text:''
			},
			upForm:{}
    }
  },
	props:['title','id'],
  methods:{
		uptosearchdialog:function(){
			this.upForm[this.id]=String.trim(this.textForm.text)
			this.$emit('fromsearchitem',this.upForm)
		},
		cleanf:function(val){
			this.textForm=common.clone(this.tempForm)
			this.upForm={}
		}
  }

}
</script>