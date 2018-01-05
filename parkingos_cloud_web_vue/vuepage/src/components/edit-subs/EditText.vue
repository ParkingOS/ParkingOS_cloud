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
	props:['id','rowdata'],

  methods:{
		uptoeditdialog:function(){
			if(this.textForm.text!=null&&typeof(this.textForm.text)!='undefined'){
				this.upForm[this.id]=String.trim(this.textForm.text)
			}
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(val){
			for(var x in this.rowdata){
				if(this.id==x){
					this.textForm.text=this.rowdata[x]
				}
			}
		},
		cleanf:function(val){
			this.textForm=common.clone(this.tempForm)
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