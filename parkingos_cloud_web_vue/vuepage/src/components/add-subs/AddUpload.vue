<template>
  <div class="upload" style="margin-right: 15px">
				<el-upload
					class="upload-demo"
					:action="action"
					:before-upload="beforeUpload"
					:on-preview="handlePreview"
					:on-remove="handleRemove"
					:on-success="handlesuccess"
					:on-error="handleError"
					:on-change="handleChange"
					:file-list="fileList"
					>
					<el-button size="small" type="primary">点击上传</el-button>
					<div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过100kb</div>
				</el-upload>				
  </div>
</template>
 
<script>
import common from '../../common/js/common.js'
import {path} from '../../api/api'
export default {
  name: 'upload',
  data () {
    return {
			 fileList:[],
			 tempFilelist:[],
			 fileLength:'',
			 upForm:{},
			 action:path+'/upload/dbfile?token='+sessionStorage.getItem('token')
    }
  },
	props:['id'],

  methods:{
		handlePreview(){

		},
		handleRemove(){

		},
		beforeUpload(file){
			const isPNG = file.type==='image/png'
			const isJPG = file.type==='image/jpeg'
			const isLt100k = file.size/1024 < 100
			if(!isPNG&&!isJPG){
				this.$message.error('上传logo只能是PNG或JPG格式!');
			}else if(!isLt100k){
				this.$message.error('上传logo大小不能超过100KB!');
			} 
			return (isJPG||isPNG)&&isLt100k
		},
		handleChange(file,fileList){
			this.fileList = fileList.slice(-1)
		},
		handleError(err,file,fileList){
			//console.log(err)
			this.$message.error('上传失败,请重新上传!');
		},
		handlesuccess(response, file, fileList){
			  if(response.state){
				 this.upForm[this.id]=response.file_id
				 this.$emit('fromedititem',this.upForm)
			 	}else{
					 //上传失败
						this.upForm[this.id]=''
						this.$emit('fromedititem',this.upForm)
						this.fileList = {}
						this.upForm={}
					  this.$message.error('上传失败,请重新上传!');
				}
		},
		setValue(){
				this.fileList=common.clone(this.tempFilelist)
				this.upForm={}
		},
		cleanf(){
			
		}
	
  },

}
</script>