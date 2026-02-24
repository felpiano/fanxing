const API = (function() {
  const keys = {
    baseUrl: 'https://cfxpay.jszdf.xyz/api',
  };
  Object.freeze(keys);
  return keys;
}())

const API_URL = (function() {
  const urlMap = {
    getOrderData: '/order/getOrderData',
    setOrderPayer: '/order/setOrderPayer',
    upload: '/order/upload',
    ploadReceiptImg: '/order/uploadReceiptImg'
  };
  for (const key in urlMap) {
    urlMap[key] = API.baseUrl + urlMap[key]
  }
  return urlMap;
})()


function getUrlParam (href) {
  const query = {};
  const search = href.substring(href.lastIndexOf('?') + 1);
  const arr = search.split('&');
  for (let i = 0; i < arr.length; i++) {
    const key = arr[i].split('=')[0];
    query[key] = arr[i].split('=')[1]
  }
  return query
}
