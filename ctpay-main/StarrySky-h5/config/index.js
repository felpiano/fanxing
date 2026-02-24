const config = {
	production: {
		apiUrl: 'https://admin.jszdf.xyz/api',
		wsUrl: 'wss:/admin.jszdf.xyz'
	},
	development: {
		apiUrl: 'https://admin.jszdf.xyz/api',
		wsUrl: 'wss:/admin.jszdf.xyz'
	}
}
const env = process.env.NODE_ENV
module.exports = {
	...config[env]
}
