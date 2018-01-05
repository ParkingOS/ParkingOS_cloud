<template>
  <div class="number" style="margin-right: 15px">
				<el-input type="textarea" :rows="4" v-model="textareaForm.textarea" @blur="uptoeditdialog"></el-input>
  </div>
</template>

<script>
import common from '../../common/js/common.js'
export default {
  name: 'textarea',
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
	props:['id','rowdata'],

  methods:{
		uptoeditdialog:function(){
			this.upForm[this.id]=String.trim(this.textareaForm.textarea)
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(val){
			for(var x in this.rowdata){
				if(this.id==x){
					this.textareaForm.textarea=this.rowdata[x]
				}
			}
		},
		cleanf:function(val){
			this.textareaForm=common.clone(this.tempForm)
			this.upForm={}
		}
  },
	watch:{
		'rowdata':function(){
			console.log('watch:text')
			this.setValue()
			this.uptoeditdialog()
		}
	},
	mounted(){
		this.setValue()
		this.uptoeditdialog()
	}

}
</script>