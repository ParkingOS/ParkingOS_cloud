<template>
  <div class="number" style="margin-right: 15px">
				<el-input type="textarea" :rows="4" v-model="textareaForm.textarea" @blur="uptoeditdialog" ></el-input>
  </div>
</template>

<script>
import common from '../../common/js/common.js'
export default {
  name: 'text',
  data () {
    return {
			textareaForm:{
				textarea:''
			},
			tempForm:{
				textarea:''
			},
			upForm:{}
    }
  },
	props:['id'],

  methods:{
		uptoeditdialog:function(){
			this.upForm[this.id]=String.trim(this.textareaForm.textarea)
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
				this.textareaForm=common.clone(this.tempForm)
				this.upForm={}
		}
  },

}
</script>