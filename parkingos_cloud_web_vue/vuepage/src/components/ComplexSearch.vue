<template>
  <div class="complexsearch">
    <el-dialog :title="title" v-model="searchVisible" @close="onclose" :close-on-click-modal="false" top="8%"  size="tiny">
			<div v-for="items in searchitems">
				<p 
					v-for="item in items.subs"
					v-if="item.searchable"
					:is="item.type"
					:id="item.prop"
					:ref="item.prop"
					:title="item.label"
					:selectlist="item.selectlist"
					v-on:fromsearchitem="listensearchitem"
				></p>
			</div>
			
			<el-col :span="24" align="right" style="margin-bottom: 12px;margin-top: 5px">
				<el-button @click="resetSearch" size="small">清空</el-button>
				<el-button type="primary" @click="searchSubmit" :loading="addLoading" size="small">搜索</el-button>
			</el-col>
		</el-dialog>
  </div>
</template>

<script>
import common from '../common/js/common.js'
import Number from './subs/Number'
import Date from './subs/Date'
import Text from './subs/Text'
import Select from './subs/Select'
import Vue from 'vue'

export default {
	components:{
		number:Number,
		str:Text,
		date:Date,
		selection:Select,
		multitext:Text
	},
	data () {
		return {
			searched:false,
			addLoading:false,
			searchForm:{},
			tempSearchForm:{},
		}
	},
	props:['searchVisible','searchitems','title'],
  	methods:{
		listensearchitem:function(formitem){
			//searchForm中存在则覆盖，不存在则加入该属性
			this.$extend(this.searchForm,formitem)
		},
		onclose(){
			//是否查询了
			if(!this.searched){
				this.resetSearch()
			}
			//关闭对话框
			this.$emit('searchdialog',false)
		},
		resetSearch(){
			//重置search 
			//清空所有子组件的searchForm
			//循环调用，清除所以子组件内的表单值
			for(var i=0;i<this.searchitems.length;i++){
				for(var x in this.searchitems[i].subs){
					var t=this.searchitems[i].subs[x].prop;
					if(typeof(this.$refs[t])!='undefined'){
						this.$refs[t][0].cleanf()
					}
				}
			}
			//重置汇总searchForm内数据
			this.searchForm={}
			this.searched=false
		},
		searchSubmit(){
			if(common.getLength(this.searchForm)>0){
				this.searched = true
			}
			console.log(this.searchForm)
			//将searchForm传递给父组件
			this.$emit('search',this.searchForm)
			//关闭对话框
			this.$emit('searchdialog',false)
		},
		render(){}
  },
  beforeMount(){
  },
  mounted(){
	  console.log('mount complexsearch')
  },
  destroyed(){
	  console.log('destroy complextsearch')
  }

}
</script>