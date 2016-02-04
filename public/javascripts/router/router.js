define(['backbone', 'jquery', 'model/loginModel', 'view/loginView'], function(Backbone, $, LoginModel, LoginView) {

    return Backbone.Router.extend({

        routes: {
            ""           : "login",
            "login"      : "login",
            "main"       : "main"
        },

        execute: function(callback, args, name) {
            if (!this.loggedIn()) {
                this.navigate("", {trigger: false, replace: false});
                this.login();
                return false;
            }
            if (callback) callback.apply(this, args);
        },

        login: function() {
            var loginModel;
            if (this.loggedIn()) {
                var sessionId = localStorage.getItem("sessionId");
                loginModel = new LoginModel({sessionId: sessionId});
                loginModel.login();
            } else {
                loginModel = new LoginModel();
                var loginView = new LoginView({model: loginModel});
                this.switchView(loginView, $("body"));
            }
        },

        main: function() {
            if (this.currentView !== null) this.currentView.remove();
        },

        loggedIn: function() {
            var sessionId = localStorage.getItem("sessionId");
            return sessionId !== undefined && sessionId !== null;
        },

        currentView: null,

        switchView: function(newView, targetElement) {
            if (this.currentView !== null) {
                this.currentView.remove();
            }
            newView.render();
            targetElement.html(newView.el);
            this.currentView = newView;
        }

    });
});