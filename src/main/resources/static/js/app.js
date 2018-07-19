const Vue = require('vue');
const foundation = require('foundation-sites');
const $ = require('jquery');

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
            $(this.$el).removeClass("hide");
            $(this.$el).foundation();
        },
		destroyed: function () {
		}
	});
})();
