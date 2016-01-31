define(['backbone', 'underscore', 'text!../../template/login.html'], function(Backbone, _, loginTemplate) {

    return Backbone.View.extend({

        tagName: "div",
        className: "container",
        template: _.template(loginTemplate),

        events: {
            "click #loginBtn": "handleLogin"
        },

        render: function() {
            this.$el.html(this.template(this.model));
            return this;
        },

        handleLogin: function() {
            var email = this.$("#inputEmail").val();
            var password = this.$("#inputPassword").val();
            this.model.set({email: email, password: password});
            this.model.login();
        }

    });
});