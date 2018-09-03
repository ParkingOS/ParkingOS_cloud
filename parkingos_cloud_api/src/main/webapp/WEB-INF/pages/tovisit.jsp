<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=utf-8"
		 pageEncoding="utf-8"%>
<html>

	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
		<title>来访车辆</title>
		<link rel="stylesheet" href="https://cdn.bootcss.com/weui/1.1.2/style/weui.min.css">
		<link rel="stylesheet" href="https://cdn.bootcss.com/jquery-weui/1.2.0/css/jquery-weui.min.css">
		<link rel="stylesheet" type="text/css" href="https://unpkg.com/mint-ui/lib/style.css" />
		<style>
			.demos-title {
				text-align: center;
				font-size: 34px;
				color: #3cc51f;
				font-weight: 400;
				margin: 0 20px;
			}
			
			.required-flag {
				color: red;
			}
		</style>
		<style type="text/css">
			.carnum-input {
				text-align: center;
				font-size: 17px;
				height: 17px;
				padding: 4px 0;
				width: 20px;
				font-size: 17px;
				margin: 0px;
				vertical-align: middle;
				border-radius: 5px;
				text-decoration: none;
				border-color: #888888;
				border-top-width: 1px;
				border-right-width: 1px;
				border-bottom-width: 1px;
				border-left-width: 1px;
				border-style: dotted;
			}
			
			.inp-color {
				padding: 0;
				width: 23px;
				height: 23px;
				border-radius: 50%;
				background: #1aad19;
				color: #fff;
				font-size: 17px;
				font-weight: bolder;
			}
			
			.info {
				display: none
			}
			
			.mint-button--normal {
				padding: 0 0px;
				margin: 2px 4px;
				width: 46px;
				text-align: center;
			}
			
			.keyboard {
				margin: 0px;
				padding: 0px 1px;
				position: absolute;
				bottom: 0px;
				left: 0px;
				right: 0px;
				padding: 5px 1px;
				background: #C4C9C9;
				z-index: 99;
			}
			
			.carnum-select {
				border-bottom-color: #1AAD19;
				border-bottom-width: 1px;
			}
			
			.mint-navbar .mint-tab-item.is-selected {
				border-bottom: 3px solid #1AAD19;
				color: #1AAD19;
			}
			
			.weui-dialog__btn {
				display: block;
				-webkit-box-flex: 1;
				-webkit-flex: 1;
				flex: 1;
				color: #1AAD19;
				text-decoration: none;
				-webkit-tap-highlight-color: rgba(0, 0, 0, 0);
				position: relative;
			}
			
			.mint-button--primary {
				color: #fff;
				background-color: #1AAD19;
			}
			
			.mint-button {
				font-size: 22px;
				height: 38px;
			}
			
			.keyboard {
				padding-top: 8px;
				padding-bottom: 10px;
				padding-left: 0px;
				padding-right: 0px;
			}
			
			.mint-button--default {
				color: #656b79;
				background-color: white;
				box-shadow: 1px 1px 1px #8E9393;
			}
			
			.mint-button--normal {
				margin: 2px 2px;
			}
		</style>
	</head>

	<body ontouchstart>
		<div id="app">
			<header class='demos-header'>
				<h1 class="demos-title">访客信息登记</h1>
			</header>
			<div class="weui-cells weui-cells_form">
				<div class="weui-cell">
					<div class="weui-cell__hd"><label class="weui-label"><sup class="required-flag">*</sup>车牌号</label></div>
					<div class="weui-cell__bd">

						<!--<input id="car_number" class="weui-input required" type="text" placeholder="请输入车牌号">-->
						<div id="keys" @click="keys">
							<input id="carnum1" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum2" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum3" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum4" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum5" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum6" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum7" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input id="carnum8" v-show="selected==2" max-length="1" onfocus="this.blur();" class="carnum-input" value="">
							<input id="carnum9" v-show="false" max-length="1" onfocus="this.blur();" class="carnum-input" value=""><input @click="addNew" onfocus="this.blur();" class="carnum-input inp-color" v-model="addstr" />
						</div>
					</div>
				</div>

				<div class="weui-cell">
					<div class="weui-cell__hd"><label for="" class="weui-label"><sup class="required-flag">*</sup>开始时间</label></div>
					<div class="weui-cell__bd">
						<input id="begin_time" @focus="focus" @click="openPicker" v-model="visitorForm.begin_time" class="weui-input required" type="button" style="text-align: left;">
						<mt-datetime-picker 
							ref="picker"
							 type="datetime"
							 v-model="pickerValue"
							  month-format="{value} 月"
							  date-format="{value} 日"
							  hour-format="{value}时"
							  minute-format="{value}分"
							  visible-item-count="5"
							  @confirm="handleConfirm">
						</mt-datetime-picker>
					</div>
				</div>
				<div class="weui-cell">
					<div class="weui-cell__hd"><label for="" class="weui-label"><sup class="required-flag">*</sup>结束时间</label></div>
					<div class="weui-cell__bd">
						<input @focus="focus" @click="openPicker2" v-model="visitorForm.end_time" id="end_time" class="weui-input required" type="button" style="text-align: left;">
						<mt-datetime-picker 
							ref="picker2"
							 type="datetime"
							  v-model="pickerValue2"
							  year-format="{value}"
							  month-format="{value} 月"
							  date-format="{value} 日"
							  hour-format="{value}时"
							  minute-format="{value}分"
							  visible-item-count="5"
							   @confirm="handleConfirm2">
						</mt-datetime-picker>
					</div>
				</div>
				<div class="weui-cell">
					<div class="weui-cell__hd"><label for="" class="weui-label">备注</label></div>
					<div class="weui-cell__bd">
						<textarea @focus="focus" maxlength ="20" v-model="visitorForm.remark" id="remarks" class="weui-textarea" placeholder="请输入备注" rows="3"></textarea>
						<div class="weui-textarea-counter"><span v-text="20- result"></span>/20</div>
					</div>
				</div>

				<div class="weui-cell weui-cell_vcode">
					<div class="weui-cell__hd">
						<label class="weui-label"><sup class="required-flag">*</sup>手机号</label>
					</div>
					<div class="weui-cell__bd">
						<input @focus="focus" v-model="visitorForm.mobile" id="ipone" class="weui-input required" type="tel" placeholder="请输入手机号">
					</div>
					<div class="weui-cell__ft">
						<button id="getVerifyingCode" @click="getVerifyingCode" class="weui-vcode-btn">获取验证码</button>
					</div>
				</div>
				<div class="weui-cell">
					<div class="weui-cell__hd"><label class="weui-label"><sup class="required-flag">*</sup>验证码</label></div>
					<div class="weui-cell__bd">
						<input @focus="focus" id="code" class="weui-input required" type="text" placeholder="请输入验证码">
					</div>
				</div>
			</div>
			<div class="weui-btn-area">
				<button class="weui-btn weui-btn_primary" :disabled="isDisabled" @focus="focus" @click="showTooltips" id="showTooltips">确定</button>
			</div>

			<!--键盘-->
			<div class="keyboard" v-show="showChar" align="center">
				<div id="chars">
					<div style="display:inline;" v-for="char1 in chars1">
						<mt-button :style="charStyle" @click.native="handleClick(char1.name)" :id="char1.id" v-text="char1.name"></mt-button>
					</div>
					<div style="height:5px"></div>
					<div style="display:inline;" v-for="char2 in chars2">
						<mt-button :style="charStyle" @click.native="handleClick(char2.name)" :id="char2.id" v-text="char2.name"></mt-button>
					</div>
					<div style="height:5px"></div>
					<div style="display:inline;" v-for="char3 in chars3">
						<mt-button :style="charStyle" @click.native="handleClick(char3.name)" :id="char3.id" v-text="char3.name"></mt-button>
					</div>
					<div style="height:5px"></div>
					<div style="display:inline;" v-for="char4 in chars4">
						<mt-button :style="charStyle" @click.native="handleClick(char4.name)" :id="char4.id" v-text="char4.name"></mt-button>
					</div>
				</div>
			</div>
			<div class="keyboard" v-show="showLetter">
				<div id="letters">
					<div align="center" style="text-align:center">
						<div style="display:inline" v-for="number in numbers">
							<mt-button :style="numberStyle" @click="handleClick(number.name)" v-text="number.name"></mt-button>
						</div>
						<div style="height:5px"></div>
						<div style="display:inline;" v-for="letter1 in letters1">
							<mt-button :style="letter1Style" @click="handleClick(letter1.name)" v-text="letter1.name"></mt-button>
						</div>
						<div style="height:5px"></div>
						<div style="display:inline;" v-for="letter2 in letters2">
							<mt-button :style="letter1Style" @click="handleClick(letter2.name)" v-text="letter2.name"></mt-button>
						</div>
						<div style="height:5px"></div>
						<div style="display:inline;" v-for="letter3 in letters3">
							<mt-button :style="letter1Style" @click="handleClick(letter3.name)">
								<div v-if="letter3.name=='DEL'" v-text="jt"></div>
								<div v-if="letter3.name!='DEL'" v-text="letter3.name"></div>
							</mt-button>
						</div>
					</div>
				</div>
			</div>
		</div>

		<script src="https://cdn.bootcss.com/jquery/2.2.4/jquery.min.js"></script>
		<script src="https://cdn.bootcss.com/jquery-weui/1.2.0/js/jquery-weui.min.js"></script>
		<script src="https://cdn.bootcss.com/vue/2.5.16/vue.min.js"></script>
		<script src="https://unpkg.com/mint-ui/lib/index.js"></script>
		<script type="text/javascript">
			//var path = "http://test.bolink.club/zld"
			var path = "${pageContext.request.contextPath}";
		</script>
		<script>
			var carnumber = ""
			new Vue({
				el: '#app',
				data: function() {
					return {
						jt:'←',
						pickerValue: "",
						pickerValue2: "",
						isDisabled: false,
						addstr: "+",
						visitorForm: {
							car_name: "",
							begin_time: "",
							end_time: "",
							remark: "",
							mobile: "",
							comid: ""
						},
						selected: "1",
						chars1: [{
								"id": "1",
								"name": "京"
							},
							{
								"id": "2",
								"name": "沪"
							},
							{
								"id": "3",
								"name": "浙"
							},
							{
								"id": "4",
								"name": "粤"
							},
							{
								"id": "5",
								"name": "苏"
							},
							{
								"id": "6",
								"name": "鲁"
							},
							{
								"id": "7",
								"name": "晋"
							},
							{
								"id": "34",
								"name": "吉"
							},
							{
								"id": "8",
								"name": "冀"
							},
							{
								"id": "9",
								"name": "豫"
							},
						],
						chars2: [{
								"id": "10",
								"name": "川"
							},
							{
								"id": "11",
								"name": "渝"
							},
							{
								"id": "12",
								"name": "辽"
							},
							{
								"id": "13",
								"name": "黑"
							},
							{
								"id": "14",
								"name": "皖"
							},
							{
								"id": "15",
								"name": "鄂"
							},
							{
								"id": "16",
								"name": "湘"
							},
							{
								"id": "17",
								"name": "赣"
							},
							{
								"id": "18",
								"name": "闽"
							},
						],
						chars3: [{
								"id": "19",
								"name": "陕"
							},
							{
								"id": "20",
								"name": "甘"
							},
							{
								"id": "21",
								"name": "宁"
							},
							{
								"id": "22",
								"name": "蒙"
							},
							{
								"id": "23",
								"name": "津"
							},
							{
								"id": "26",
								"name": "桂"
							},
							{
								"id": "25",
								"name": "云"
							},
							{
								"id": "24",
								"name": "贵"
							},
						],
						chars4: [{
								"id": "27",
								"name": "琼"
							},
							{
								"id": "28",
								"name": "青"
							},
							{
								"id": "29",
								"name": "新"
							},
							{
								"id": "30",
								"name": "藏"
							},
							{
								"id": "31",
								"name": "使"
							},
						],
						numbers: [{
								"id": "100",
								"name": "0"
							},
							{
								"id": "101",
								"name": "1"
							},
							{
								"id": "102",
								"name": "2"
							},
							{
								"id": "103",
								"name": "3"
							},
							{
								"id": "104",
								"name": "4"
							},
							{
								"id": "105",
								"name": "5"
							},
							{
								"id": "106",
								"name": "6"
							},
							{
								"id": "107",
								"name": "7"
							},
							{
								"id": "108",
								"name": "8"
							},
							{
								"id": "109",
								"name": "9"
							},
						],
						letters1: [{
								"id": "50",
								"name": "A"
							},
							{
								"id": "51",
								"name": "B"
							},
							{
								"id": "52",
								"name": "C"
							},
							{
								"id": "53",
								"name": "D"
							},
							{
								"id": "54",
								"name": "E"
							},
							{
								"id": "55",
								"name": "F"
							},
							{
								"id": "56",
								"name": "G"
							},
							{
								"id": "57",
								"name": "H"
							},
							{
								"id": "58",
								"name": "J"
							},
							{
								"id": "59",
								"name": "K"
							},
						],
						letters2: [{
								"id": "60",
								"name": "L"
							},
							{
								"id": "61",
								"name": "M"
							},
							{
								"id": "62",
								"name": "N"
							},
							{
								"id": "63",
								"name": "P"
							},
							{
								"id": "64",
								"name": "Q"
							},
							{
								"id": "65",
								"name": "R"
							},
							{
								"id": "66",
								"name": "S"
							},
							{
								"id": "67",
								"name": "T"
							},
							{
								"id": "68",
								"name": "U"
							},
							{
								"id": "69",
								"name": "V"
							},
						],
						letters3: [{
								"id": "74",
								"name": "港"
							},
							{
								"id": "75",
								"name": "澳"
							},
							{
								"id": "70",
								"name": "W"
							},
							{
								"id": "71",
								"name": "X"
							},
							{
								"id": "72",
								"name": "Y"
							},
							{
								"id": "73",
								"name": "Z"
							},
							{
								"id": "76",
								"name": "学"
							},
							{
								"id": "77",
								"name": "领"
							},
							{
								"id": "99",
								"name": "DEL"
							},
							//{"id":"98","name":"关闭"},
						],
						keyboard: '',
						btn: '',
						showChar: '',
						showLetter: '',
						selected1: '',
						selected2: '',
						selected3: '',
						selected4: '',
						selected5: '',
						selected6: '',
						selected7: '',
						selected8: '',
						cNode: 'carnum1',
						numberStyle: '',
						letter1Style: '',
						letter3Style: '',
						charStyle: '',
					}

				},
				methods: {
					checkLength:function(index){
	                    return index <= 1 ? 20: ''
	                },
					format: function(time, format) {
						var t = new Date(time);
						var tf = function(i) {
							return(i < 10 ? '0' : '') + i
						};
						return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a) {
							switch(a) {
								case 'yyyy':
									return tf(t.getFullYear());
									break;
								case 'MM':
									return tf(t.getMonth() + 1);
									break;
								case 'mm':
									return tf(t.getMinutes());
									break;
								case 'dd':
									return tf(t.getDate());
									break;
								case 'HH':
									return tf(t.getHours());
									break;
								case 'ss':
									return tf(t.getSeconds());
									break;
							}
						})
					},
					handleConfirm: function(value) {
						console.log(value)
						this.visitorForm.begin_time = this.format(value,'yyyy-MM-dd HH:mm:ss')
					},
					openPicker: function() {
						this.showChar = false;
						this.showLetter = false;
						this.$refs.picker.open();
					},
					openPicker2: function() {
						this.showChar = false;
						this.showLetter = false;
						this.$refs.picker2.open();
					},
					handleConfirm2: function(value) {
						console.log(value)
						this.visitorForm.end_time = this.format(value,'yyyy-MM-dd HH:mm:ss')
					},
					//新增
					addNew: function() {
						if(this.selected == 1) {
							this.selected = 2;
							this.addstr = "-"
						} else {
							this.selected = 1;
							this.addstr = "+"
						}

					},
					//提交
					showTooltips: function() {
						var that = this;
						var ipone = that.visitorForm.mobile
						that.confirm()
						if(carnumber != "" && carnumber.length == 7 || carnumber.length == 8) {
							var len = $(".required").length
							//								ipone = $("#ipone").val();
							for(var i = 0; i < len; i++) {
								var val = $(".required").eq(i).val()
								if(val == "") {
									var tooltips = $(".required").eq(i).parents(".weui-cell").find(".weui-label").text()
									$.alert(tooltips + "不能为空","提示")
									return;
								}
							}
							if(!(/^1[345789]\d{9}$/.test(ipone))) {
								$.alert("请输入正确的手机号","提示")
								return;
							} else {
								var code = $("#code").val()
								$.post(path + "/visitor/validcode?mobile=" + ipone + "&code=" + code, function(e) {
									if(e == 1) {
										$.ajax({
											type: 'post',
											url: path + "/visitor/addvistor",
											data: {
												mobile: ipone,
												car_number: carnumber,
												begin_time: that.visitorForm.begin_time,
												end_time: that.visitorForm.end_time,
												remark: that.visitorForm.remark,
												comid: "${comid}"
											},
											success: function(e) {
												that.isDisabled = true;
												var url = "success?type=1"
												window.location.href = url
											},
											error: function(e) {
												var url = "success?type=0"
												window.location.href = url
											}
										})
									} else {
										$.alert("您输入的验证码不正确","提示")
									}
								})
							}
						}

					},
					//获取验证码
					getVerifyingCode: function() {
						var that = this;
						var tel = that.visitorForm.mobile;
						if(!(/^1[345789]\d{9}$/.test(tel))) {
							$.alert("请输入正确的手机号","提示")
							that.visitorForm.mobile = "";
						} else {
							$("#getVerifyingCode").attr("disabled", true);
							$.post(path + "/visitor/sendcode?mobile=" + tel, function(e) {
								if(e == 1) {
									that.invokeSettime("#getVerifyingCode");
									$.alert("发送成功!","提示")
								} else {
									$.alert("发送失败，请重新发送","提示")
									$("#getVerifyingCode").attr("disabled", false);
								}
							})
						}
					},
					focus: function() {
						this.showChar = false;
						this.showLetter = false

					},
					keys: function() {
						if(this.showChar || this.showLetter) {
							return false;
						} else {
							if($("#carnum1").val() == "") {
								//显示汉字,隐藏字母
								this.showChar = true;
								this.showLetter = false
							} else {
								//显示字母,隐藏汉字
								this.showChar = false;
								this.showLetter = true
							}

						}
					},
					handleClick: function(name) {
						console.log(name)
						var value = name
						if(name == '关闭') {
							this.showLetter = false;
							this.showChar = false;
						} else if(name == 'DEL') {
							//清除当前节点值
							var cur = document.getElementById(this.cNode)
							this.removeClass(cur, "carnum-select")
							var i = parseInt(this.cNode.substr(6)) - 1;
							if(i == 1) {
								//显示汉字,隐藏字母
								this.showChar = true;
								this.showLetter = false;
							} else if(i < 1) {
								i = 1
							}
							this.cNode = "carnum" + i
							var last = document.getElementById(this.cNode)
							last.value = "";
							this.addClass(document.getElementById(this.cNode), "carnum-select")
						} else {
							//
							var cur = document.getElementById(this.cNode)
							var i = parseInt(this.cNode.substr(6))
							if(this.selected == "1") {
								if(i >= 8) {
									i = 8
								} else {
									cur.value = value;
									i += 1;
								}
							} else {
								if(i >= 9) {
									i = 9
								} else {
									cur.value = value;
									i += 1;
								}
							}

							this.removeClass(cur, "carnum-select")
							this.cNode = "carnum" + i
							this.addClass(document.getElementById(this.cNode), "carnum-select")
							if(i == 2) {
								this.showChar = false;
								this.showLetter = true;
							}
						}

					},
					carnum: function(id) {
						var k = id.substr(6)
						this.cNode = id;
						for(var i = 1; i <= 8; i++) {
							if(i == k) {
								this.addClass(document.getElementById("carnum" + i), "carnum-select")
							} else {
								this.removeClass(document.getElementById("carnum" + i), "carnum-select")
							}
						}
						console.log(k)
						if(k == '1') {
							//显示汉字,隐藏字母
							this.showChar = true;
							this.showLetter = false
						} else {
							//反之
							this.showChar = false;
							this.showLetter = true
						}
					},
					confirm: function() {
						//获取完整车牌
						var sum = "7"
						carnumber = ""
						if(this.selected == "2") {
							sum = "8"
						}
						var submitable = true;
						for(var i = 1; i <= sum; i++) {
							var carnum = document.getElementById("carnum" + i)
							if(carnum.value == "" || typeof(carnum.value) == "undefined") {
								$.alert("车牌号不正确!","提示")
								submitable = false;
								break;
							}
							if(i == 2) {
								var m = /^[A-Z]{1}$/;
								if(!carnum.value.match(m)) {
									$.alert("车牌号不正确!","提示")
									submitable = false;
									break;
								}
							}
							if(i > 2 && i < sum) {
								var m2 = /^[0-9A-Z]{1}$/;
								if(!carnum.value.match(m2)) {
									$.alert("车牌号不正确!","提示")
									submitable = false;
									break;
								}
							}
							carnumber += carnum.value
						}
						return carnumber
					},
					//每次添加一个class
					addClass: function(currNode, newClass) {
						var oldClass;
						oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
						if(oldClass !== null) {
							newClass = oldClass + " " + newClass;
						}
						currNode.className = newClass; //IE 和FF都支持
					},
					//每次移除一个class
					removeClass: function(currNode, curClass) {
						var oldClass, newClass1 = "";
						oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
						if(oldClass !== null) {
							oldClass = oldClass.split(" ");
							for(var i = 0; i < oldClass.length; i++) {
								if(oldClass[i] != curClass) {
									if(newClass1 == "") {
										newClass1 += oldClass[i]
									} else {
										newClass1 += " " + oldClass[i];
									}
								}
							}
						}
						currNode.className = newClass1; //IE 和FF都支持
					},
					invokeSettime: function(obj) {
						var countdown = 60;
						settime(obj);

						function settime(obj) {
							if(countdown == 0) {
								$(obj).attr("disabled", false);
								$(obj).text("获取验证码");
								countdown = 60;
								return;
							} else {
								$(obj).attr("disabled", true);
								$(obj).text("(" + countdown + ") s 重新发送");
								countdown--;
							}
							setTimeout(function() {
								settime(obj)
							}, 1000)
						}
					}
				},
				mounted: function() {
					var _that = this;
					var w = screen.width;
					document.getElementById('app').style.display = 'block'
					//设置样式
					this.numberStyle = "width:" + w * 0.086 + "px;height: 37px;";
					this.letter1Style = "width:" + w * 0.086 + "px;height: 37px;";
					this.charStyle = "width:" + w * 0.086 + "px;height: 37px";
					//显示汉字输入,隐藏字母
					//					this.showChar = true;
					//					this.showLetter = false
					//input选中1
					var carnum1 = document.getElementById("carnum1");
					this.addClass(carnum1, "carnum-select")
					this.pickerValue = new Date()
					this.pickerValue2 = new Date()
				},
				computed:{
					result:function(){
                     var l = 0;
                     var arr = [];
                     var reg = /^[\s]$/g;	
//                        l = (this.visitorForm.remark).toString().replace(/\s/g,"").length;
                          l = (this.visitorForm.remark).toString().length;
                          arr.push(l);
                    	return arr;
                 }
				}
			})
		</script>
	</body>

</html>