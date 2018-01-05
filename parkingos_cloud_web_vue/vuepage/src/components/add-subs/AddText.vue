<template>
  <div class="number" style="margin-right: 15px">
				<el-input v-model="textForm.text" @blur="uptoeditdialog"></el-input>
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
	props:['id'],

  methods:{
		uptoeditdialog:function(){
			this.upForm[this.id]=String.trim(this.textForm.text)
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
				this.textForm=common.clone(this.tempForm)
				this.upForm={}
		}
  },

}
</script>