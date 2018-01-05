<template>
  <div class="number" style="margin-right: 15px">
    <el-col :span="24" style="margin-bottom:5px">
						<el-col :span="6" style="text-align:right;font-size:15px;padding-top:7px;padding-right:8px">
							{{title}}:
						</el-col>
						<el-col :span="5">
						<el-select v-model="numberForm.number" @change="numberchange" size="small" >
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

						<el-col :span="13" align="right" v-show="searchShow.numberstart"> 
							<el-input v-model="numberForm.number_start" @blur="uptosearchdialog" style="width:95%" size="small"></el-input>
						</el-col>
					</el-col>

					<el-col :span="24" v-show="searchShow.numberend" style="margin-bottom:5px">
						<el-col :span="13" :offset="11" align="right"> 
							<el-input v-model="numberForm.number_end" @blur="uptosearchdialog" style="width:95%" size="small"></el-input>
						</el-col>
					</el-col>
  </div>
</template>

<script>
import common from '../../common/js/common.js'

export default {
  name: 'number',
  data () {
    return {
      numberForm:{
					number:'3',
					number_start:'',
					number_end:'',
      },
			tempForm:{
					number:'3',
					number_start:'',
					number_end:'',
      },
			upForm:{},
      searchShow:{
        numberstart:true,
        numberend:false,
      },
    }
  },
	props:['title','id'],
  methods:{
    numberchange:function(val){
				common.intervalchange(this,val,'number');
				this.uptosearchdialog()
		},
		uptosearchdialog:function(){
			this.upForm[this.id]=this.numberForm.number
			this.upForm[this.id+'_start']=String.trim(this.numberForm.number_start)
			this.upForm[this.id+'_end']=String.trim(this.numberForm.number_end)
			this.$emit('fromsearchitem',this.upForm)
		},
		cleanf:function(val){
			this.numberForm=common.clone(this.tempForm)
			this.upForm={}
		}
  }

}
</script>