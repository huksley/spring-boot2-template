var Vue = require('vue');
var foundation = require('foundation-sites');

// Auth JSON for current user
var auth = window.auth;

(function () {
	new Vue({
		el: "#body", 
		data: {
			user: auth.login
		},
		computed: {
		},
		methods: {
		},
		created: function () {
		},
		mounted: function () {
		},
		destroyed: function () {
		}
	});
})();
