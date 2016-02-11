define(['backbone', 'underscore', 'text!../../template/login.html'], function(Backbone, _, loginTemplate) {

    return Backbone.View.extend({

        tagName: "div",
        className: "container",
        template: _.template(loginTemplate),

        events: {
            "click #loginBtn": "handleLogin",
            "click #toggleRegBtn": "toggleRegistration",
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

        toggleRegistration: function() {
            this.$el.find("span.nest-details").toggle();
        },

        updateModel: function() {
            var email = this.$("#inputEmail").val();
            var password = this.$("#inputPassword").val();
            var productId = this.$("#inputProductId").val();
            var productSecret = this.$("#inputProductSecret").val();
            this.model.set({email: email, password: password, productId: productId, productSecret: productSecret});
        }

    });
});