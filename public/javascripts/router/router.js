define(['backbone', 'jquery', 'model/loginModel', 'view/containerView', 'view/loginView'], function(Backbone, $, LoginModel, ContainerView, LoginView) {

    return Backbone.Router.extend({

        routes: {
            ""           : "login",
            "login"      : "login",
            "main"       : "main"
        },

        initialize: function() {
            var containerView = new ContainerView();
            this.basicView = containerView;
            this.switchView(containerView, $("body"));
        },

        execute: function(callback, args, name) {
            if (!this.loggedIn()) {
                this.navigate("", {trigger: false, replace: false});
                this.basicView.notLoggedMode();
                this.login();
                return false;
            } else {
                this.basicView.loggedMode();
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
                this.switchView(loginView, $("#bodyContent"));
            }
        },

        main: function() {
            this.switchView(new Backbone.View(), $("#bodyContent"))
            window.ws.sendMessage({action: "web_cam"});
        },

        loggedIn: function() {
            var sessionId = localStorage.getItem("sessionId");
            return sessionId !== undefined && sessionId !== null;
        },

        basicView: null,
        currentView: null,

        switchView: function(newView, targetElement) {
            if (this.currentView !== null && this.currentView !== this.basicView) {
                this.currentView.remove();
            }
            newView.render();
            targetElement.html(newView.el);
            this.currentView = newView;
        }

    });
});