<template>
  <div class="number" style="margin-right: 15px">
		<el-input v-model="numberForm.number" @blur="uptoeditdialog"> </el-input>
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
	props:['id'],
  methods:{
		uptoeditdialog:function(){
			this.upForm[this.id]=this.numberForm.number+''
			this.$emit('fromedititem',this.upForm)
		},
		setValue:function(){
			this.numberForm=common.clone(this.tempForm)
			this.upForm={}
		}

  },
}
</script>