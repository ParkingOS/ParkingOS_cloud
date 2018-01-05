// import Login from './pages/Login.vue'
import LoginCloud from './pages/LoginCloud.vue'
import NotFound from './pages/404.vue'
// import Home from './pages/Home.vue'
import HomeCloud from './pages/HomeCloud.vue'

//云平台页面
import OrderManage_Orders from './pages/park/OrderManage_Orders.vue'
import OrderManage_Poles from './pages/park/OrderManage_Poles.vue'
import MonthMember_Refill from './pages/park/MonthMember_Refill.vue'
import MonthMember_VIP from './pages/park/MonthMember_VIP.vue'
import OrderStatistics from './pages/park/OrderStatistics.vue'
import ShopManage_Coupon from './pages/park/ShopManage_Coupon.vue'
import ShopManage_Shop from './pages/park/ShopManage_Shop.vue'
import ShopManage_QueryAccount from './pages/park/ShopManage_QueryAccount.vue'
import EquipmentManage_Monitor from './pages/park/EquipmentManage_Monitor.vue'
import EquipmentManage_Intercom from './pages/park/EquipmentManage_Intercom.vue'
import EquipmentManage_WorkStation from './pages/park/EquipmentManage_WorkStation.vue'
import EquipmentManage_Channel from './pages/park/EquipmentManage_Channel.vue'
import EmployeePermission_Role from './pages/park/EmployeePermission_Role.vue'
import EmployeePermission_Manage from './pages/park/EmployeePermission_Manage.vue'
import SystemManage_BlachList from './pages/park/SystemManage_BlachList.vue'
import SystemManage_Commute from './pages/park/SystemManage_Commute.vue'
import SystemManage_Account from './pages/park/SystemManage_Account.vue'
import SystemManage_Params from './pages/park/SystemManage_Params.vue'
import SystemManage_FreeReason from './pages/park/SystemManage_FreeReason.vue'
import SystemManage_CarType from './pages/park/SystemManage_CarType.vue'
import SystemManage_Price from './pages/park/SystemManage_Price.vue'
import SystemManage_MonthCard from './pages/park/SystemManage_MonthCard.vue'
import SystemManage_Logs from './pages/park/SystemManage_Logs.vue'




let routes = [

    {
        path: '/loginCloud',
        component: LoginCloud,
        name: '',
        hidden: true
    },
    // {
    //     path: '/login',
    //     component: Login,
    //     name: '',
    //     hidden: true
    // },
    {
        path: '/404',
        component: NotFound,
        name: '',
        hidden: true
    },

    /*
     {
         path: '/',
         component: Home,
         name: '控制台',
         iconCls: 'el-icon-menu',
         hidden:true,
         children: [
             { path: '/echarts1', component: Echarts, name: '收费趋势图' },
                   { path: '/echarts2', component: Echarts, name: '车位利用率' },
                   { path: '/echarts3', component: Echarts, name: '人员设备概况' },
         ]
     },
     */
    /*
    * 以下是云平台页面
    *
    * */
    {
        path: '/',
        component: HomeCloud,
        name: '订单管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/orderManage_Orders', component: OrderManage_Orders, name: '订单记录'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '订单管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/orderManage_Poles', component: OrderManage_Poles, name: '抬杆记录'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '月卡会员管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/monthMember_Refill', component: MonthMember_Refill, name: '月卡续费记录'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '月卡会员管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/monthMember_VIP', component: MonthMember_VIP, name: '月卡会员'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '统计分析',
        iconCls: 'el-icon-document',
        leaf: true,//只有一个节点
        children: [
            {path: '/orderStatistics', component: OrderStatistics, name: '统计分析'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '商户管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/shopManage_Coupon', component: ShopManage_Coupon, name: '统计分析'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '商户管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/shopManage_Shop', component: ShopManage_Shop, name: '统计分析'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '商户管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/shopManage_QueryAccount', component: ShopManage_QueryAccount, name: '统计分析'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '设备管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/equipmentManage_Monitor', component: EquipmentManage_Monitor, name: '监控管理'},
        ]
    }, {
        path: '/',
        component: HomeCloud,
        name: '设备管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/equipmentManage_Intercom', component: EquipmentManage_Intercom, name: '对讲管理'},
        ]
    }, {
        path: '/',
        component: HomeCloud,
        name: '设备管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/equipmentManage_WorkStation', component: EquipmentManage_WorkStation, name: '工作站管理'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '设备管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/equipmentManage_Channel', component: EquipmentManage_Channel, name: '通道管理'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '员工权限',
        iconCls: 'el-icon-document',
        children: [
            {path: '/employeePermission_Role', component: EmployeePermission_Role, name: '角色管理'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '员工权限',
        iconCls: 'el-icon-document',
        children: [
            {path: '/employeePermission_Manage', component: EmployeePermission_Manage, name: '员工管理'},
        ]
    },
    {
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_BlachList', component: SystemManage_BlachList, name: '黑名单管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_Commute', component: SystemManage_Commute, name: '上下班记录'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_Account', component: SystemManage_Account, name: '账户管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_Params', component: SystemManage_Params, name: '参数管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_FreeReason', component: SystemManage_FreeReason, name: '免费原因'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_CarType', component: SystemManage_CarType, name: '车型管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_Price', component: SystemManage_Price, name: '价格管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_MonthCard', component: SystemManage_MonthCard, name: '月卡套餐管理'},
        ]
    },{
        path: '/',
        component: HomeCloud,
        name: '系统管理',
        iconCls: 'el-icon-document',
        children: [
            {path: '/systemManage_Logs', component: SystemManage_Logs, name: '系统日志'},
        ]
    },

    /*
    * 404保留页面
    * */
    {
        path: '*',
        hidden: true,
        redirect: {path: '/404'}
    }
];

export default routes;
