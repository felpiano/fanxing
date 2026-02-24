// minify.js
const fs = require('fs');
const { minify } = require('html-minifier-terser');

const input = fs.readFileSync('./jtInit.html', 'utf8');

minify(input, {
  collapseWhitespace: true,
  removeComments: true,
  minifyJS: true,
  minifyCSS: true,
}).then(output => {
  fs.writeFileSync('./goldBar.html', output);
  console.log('压缩完成，输出 goldBar.html');
});
