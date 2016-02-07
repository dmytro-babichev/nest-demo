define(['backbone', 'underscore', 'text!../../template/login.html'], function(Backbone, _, loginTemplate) {

    return Backbone.View.extend({

        tagName: "div",
        className: "container",
        template: _.template(loginTemplate),

        events: {
            "click #loginBtn": "handleLogin",
            "click #registerBtn": "handleRegister"
        },

        render: function() {
            this.$el.html(this.template(this.model));
            return this;
        },

        handleLogin: function() {
            this.updateModel()
            this.model.login();
        },

        handleRegister: function() {
            this.updateModel()
            this.model.register();
        },

        updateModel: function() {
            var email = this.$("#inputEmail").val();
            var password = this.$("#inputPassword").val();
            this.model.set({email: email, password: password});
        }

    });
});