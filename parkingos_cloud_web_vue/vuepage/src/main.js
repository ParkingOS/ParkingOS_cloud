import babelpolyfill from 'babel-polyfill'
import Vue from 'vue'
import App from './App'
import ElementUI from 'element-ui'

import VueRouter from 'vue-router'
//import Vuex from 'vuex'
import routes from './routes'
import './common/css/common.css'
import './common/css/index.css'
import jQuery from 'jquery'
import common from './common/js/common'
import axios from 'axios'
import BaiduMap from 'vue-baidu-map'
import QRCode from 'qrcode'

Vue.use(QRCode)
Vue.use(ElementUI)
Vue.use(VueRouter)
Vue.use(BaiduMap,{
  ak:'Ona5elzlpSrwOeeAI1k0EE2yHtpLoxlN'
})
//Vue.use(Vuex)

Vue.prototype.QRCode=QRCode
Vue.prototype.axios=axios
Vue.prototype.$=jQuery
Vue.prototype.$post=jQuery.post
Vue.prototype.$ajax=jQuery.ajax
Vue.prototype.$getJSON=jQuery.getJSON
Vue.prototype.$extend=jQuery.extend

Vue.prototype.common=common

//NProgress.configure({ showSpinner: false });

/*
const router = new VueRouter({
  mode:'history',
  routes:routes
})
*/

const router = new VueRouter({
    routes
})

router.beforeEach((to, from, next) => {
  //NProgress.start();
  if (to.path == '/loginCloud') {
    sessionStorage.removeItem('user');
  }
  let user = JSON.parse(sessionStorage.getItem('user'));
  if (!user && to.path != '/loginCloud') {
    next({ path: '/loginCloud' })
  } else {
    next()
  }
})

//router.afterEach(transition => {
//NProgress.done();
//});

new Vue({
  //el: '#app',
  //template: '<App/>',
  router,
  //store,
  //components: { App }
  render: h => h(App)
}).$mount('#app')
