<template>
  <div class="date" style="margin-right: 15px">
    <el-col :span="24" style="margin-bottom:5px">
						<el-col :span="6" style="text-align:right;font-size:15px;padding-top:7px;padding-right:8px">
							{{title}}:
						</el-col>
						<el-col :span="5">
						<el-select v-model="dateForm.date" @change="datechange" size="small">
							<!--
								1  大于等于
								between 区间
								3  等于
								2  小于等于
								null 为空
							-->
							<el-option label="大于等于" value="1"></el-option>
     						<el-option label="区间" value="between"></el-option>
     						<el-option label="等于" value="3"></el-option>
     						<el-option label="小于等于" value="2"></el-option>
     						<!--<el-option label="为空" value="null"></el-option>-->
						</el-select>
						</el-col>

						<el-col :span="13" align="right" v-show="searchShow.datestart"> 
							<el-date-picker
								v-model="dateForm.date_start"
								type="datetime"
								:editable="false"
								style="width:95%"
								size="small"
								@change="uptosearchdialog"
								placeholder="选择日期时间">
							</el-date-picker>
							</el-col>
						</el-col>
					</el-col>

					<el-col :span="24" v-show="searchShow.dateend" style="margin-bottom:5px">
						<el-col :span="13" :offset="11" align="right"> 
								<el-date-picker
								v-model="dateForm.date_end"
								:editable="false"
								type="datetime"
								style="width:95%"
								size="small"
								@change="uptosearchdialog"
								placeholder="选择日期时间">
							</el-date-picker>
						</el-col>
					</el-col>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'date',
  data () {
    return {
      dateForm:{
					date:'3',
					date_start:'',
					date_end:'',
      },
			tempForm:{
				date:'3',
				date_start:'',
				date_end:'',
			},
			upForm:{},
      searchShow:{
        datestart:true,
        dateend:false,
      },
    }
  },
	props:['title','id'],
  methods:{
    datechange:function(val){
				common.intervalchange(this,val,'date');
				this.uptosearchdialog()
		},
		uptosearchdialog:function(){
			
			this.upForm[this.id]=this.dateForm.date;
			if(typeof(this.dateForm.date_start)=='object')
				this.upForm[this.id+'_start']=this.dateForm.date_start.getTime();
			if(typeof(this.dateForm.date_end)=='object')	
				this.upForm[this.id+'_end']=this.dateForm.date_end.getTime();
			
			this.$emit('fromsearchitem',this.upForm)
		},
		cleanf:function(val){
			this.dateForm=common.clone(this.tempForm)
			this.upForm={}
		}
  }

}
</script>