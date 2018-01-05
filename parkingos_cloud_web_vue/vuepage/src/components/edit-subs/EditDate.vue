<template>
  <div class="date" style="margin-right: 15px">
		<el-date-picker
			v-model="dateForm.date"
			:editable="false"
			type="datetime"
			style="width:95%"
			@change="uptosearchdialog"
			placeholder="选择日期时间">
		</el-date-picker>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'date',
  data () {
    return {
      dateForm:{
				date:'',
      },
			tempForm:{
				date:'',
			},
			upForm:{},
    }
  },
	props:['id','rowdata'],
  methods:{
		uptosearchdialog:function(){
			
			if(this.dateForm.date!='')
				this.upForm[this.id]=this.dateForm.date.getTime();
			
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
			if(typeof(this.rowdata)=='object'){
				for(var x in this.rowdata){
					if(this.id==x){
						this.dateForm.date=this.rowdata[x]
					}
				}
			}else{
				this.dateForm.date=''
			}
		},
		cleanf:function(val){
			this.dateForm=common.clone(this.tempForm)
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
		this.setValue()
		this.uptoeditdialog()
	}


}
</script>