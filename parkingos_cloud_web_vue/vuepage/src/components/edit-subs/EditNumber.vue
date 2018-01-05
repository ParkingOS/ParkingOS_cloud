<template>
  <div class="number" style="margin-right: 15px">
		<el-input v-model="numberForm.number" @blur="uptoeditdialog"></el-input>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'number',
  data () {
    return {
      numberForm:{
					number:'',
      },
			tempForm:{
					number:'',
      },
			upForm:{},
    }
  },
	props:['id','rowdata'],
  methods:{
		uptoeditdialog:function(){
			this.upForm[this.id]=Number(String.trim(this.numberForm.number+''));
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
			for(var x in this.rowdata){
				if(this.id==x){
					this.numberForm.number=Number(this.rowdata[x])
				}
			}
		},
		cleanf:function(val){
			this.numberForm=common.clone(this.tempForm)
			this.upForm={}
		}

  },
	watch:{
		'rowdata':function(){
			this.setValue()
			//触发事件,向编辑组件传递表单值
			this.uptoeditdialog()
		}
	},
	mounted(){
		//初始化时设置表单值
		this.setValue()
		this.uptoeditdialog()
	}

}
</script>