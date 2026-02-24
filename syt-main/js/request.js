const API = (function() {
  var keys = {
    // baseUrl: 'http://192.168.1.103:8080',
    baseUrl: 'https://admin.kmsfkm.xyz/api',
    pkey: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlNhFxwwma6deBnkWtI8MjLiy5ytvvA8sq8X6Hb7feQARFMbQoYGLfJ2jrGqmz6oCq7o5FpfrlnUHrTJc+cv5PTHHFnJ8qX9cCYLm70dQA/gZgt4ogWkMlVxFC8zUbgbd70ZwOMdDHl0mwM1CfU1+sbdAxf6Ad/pUm4E0zvzCuFf8wTZq4MBg32bSrMpsa7SibTxf92VGra6tTi3EwJDTQQM/CnE9OK22vMQ2MDMTJPSuFm261Du55ylujQLRei19co+/oDtsytNr/5VYRi5oqxZIamD1De5cJTcaXtioz3dXDELJ9PKuf+0AqqSyHvGCsobxI/8QUxAH9XKrx7RS3QIDAQAB"
  }
  Object.freeze(keys);
  return keys;
}())

const API_URL = (function() {
  var urlMap = {
    getOrderById: '/order/getOrderById',
    submitShopSubCard: '/order/shopSubCard'
  }
  for (var key in urlMap) {
    urlMap[key] = API.baseUrl + urlMap[key]
  }
  return urlMap;
})()
