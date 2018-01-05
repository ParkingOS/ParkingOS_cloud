
export const path = "http://192.168.199.122/cloud"

export const phonewidth = '150'
//beta设置为5   s.bolink设置为2 
export const percision = 5


export const paychannellist = [
    //{'value_no':'-1',"value_name":"-"},
    {'value_no': '0', "value_name": "微信"},
    {'value_no': '1', "value_name": "支付宝"},
    {'value_no': '2', "value_name": "余额"},
    {'value_no': '3', "value_name": "银联"}
]

export var checkPhone = (rule, value, callback) => {
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请输入正确的电话号码'))
    } else if (!((/^800[0-9]{7}$/.test(value)) || (/^400[0-9]{7}$/.test(value)) || (/^1[34578]\d{9}$/.test(value)) || (/^(0[0-9]{2,3}\-)([2-9][0-9]{6,7})$/.test(value)))) {
        return callback(new Error('请输入手机,座机(区号后加-),或400,800开头号码'))
    } else {
        callback()
    }
}

export var checkMobile = (rule, value, callback) => {
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请输入正确的手机号码'))
    } else if (!((/^1[34578]\d{9}$/.test(value)))) {
        return callback(new Error('请输入正确的手机号码'))
    } else {
        callback()
    }
}

export var checkURL = (rule, value, callback) => {
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请输入地址'))
    } else if (!(/(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&:/~\+#]*[\w\-\@?^=%&/~\+#])?/.test(value))) {
        return callback(new Error('请输入正确的地址'))
    } else {
        callback()
    }
}

export var checkUpload = (rule, value, callback) => {
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请上传图片'))
    } else {
        return callback()
    }
}

export var checkNumber = (rule, value, callback) => {
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请输入权重'))
    } else if (!(/^[0-9]{1,2}$/.test(value)) || value == 0) {
        return callback(new Error('权重值在1-99'))
    } else {
        callback()
    }
}

export var checkPass = (rule, value, callback) => {
    if (!(/^(\w){6,12}$/.test(value))) {
        return callback(new Error('密码为6-12位字母,数字或下划线'))
    } else {
        callback()
    }
}

export var checkCityInfo = (rule, value, callback) => {
    console.log("1111=>" + value)
    if (typeof(value) == 'undefined' || value == '') {
        return callback(new Error('请选择城市信息'))
    } else {
        callback()
    }
}

//export const distinctslist = () => { return axios.get('http://jarvisqh.vicp.io/cms-web/getdata/getdistinctlist') }