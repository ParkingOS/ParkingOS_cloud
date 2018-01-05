import fetch from '../common/js/fetch'
//beta-tcp地址
// const apiUrl = ['//120.25.207.105/bpd/']
//s-tcp地址
// const apiUrl = ['http://localhost/cms-web/']
const apiUrl = ['https://s.bolink.club/web/']
// const apiUrl = ['https://beta.bolink.club/web/']
export async function getStat() {
  const resultArr = []
  const resultJson = {}
  console.log(apiUrl)
  for (var i = 0; i < apiUrl.length; i++) {
    const url = apiUrl[i]
    const result1 = (await fetch({
      url: url + 'getconnstat',
      mode: 'cros',
      method: 'get'
    })).data
    resultArr.push(result1)
  //  console.log(i, result1)
    for (const unionId in result1) {
      const jsonObj = result1[unionId]

      // console.log(unionId, jsonObj)
      if (resultJson[unionId]) {
        console.log('xx:', resultJson[unionId])
        console.log('yy:', jsonObj)
        resultJson[unionId] = resultJson[unionId].concat(jsonObj)
      } else {
        resultJson[unionId] = jsonObj
      }
    }
  }
  // console.log(resultArr)
  // console.log(resultJson)
  // 合并同一个联盟的
  return resultJson
}

export async function getStatProfile() {
  const resultArr = []
  const resultJson = {}
  console.log("======")
  for (var i = 0; i < apiUrl.length; i++) {
    const url = apiUrl[i]
    const result1 = (await fetch({
      url: url + 'getconnstat',
      mode: 'cros',
      method: 'get'
    })).data
    resultArr.push(result1)
    console.log("======"+result1)
    for (const unionId in result1) {
      const jsonObj = result1[unionId]

      // console.log(unionId, jsonObj)
      if (resultJson[unionId]) {
        console.log('xx:', resultJson[unionId])
        console.log('yy:', jsonObj)
        resultJson[unionId] = resultJson[unionId].concat(jsonObj)
      } else {
        console.log('unionId:',jsonObj)
        resultJson[unionId] = jsonObj
      }
    }
  }
  //console.log(resultArr)
  //console.log(resultJson)
  // 合并同一个联盟的
  return resultJson
}
