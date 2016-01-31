define(['backbone'], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            email: "",
            password: ""
        },
        urlRoot: "jsRoutes.controllers.Application.login.url",
        initialize: function() {
            console.log("Model initialized");
        },
        login: function() {
            var credentials = {email: this.get("email"), password: this.get("password"), action: "login"};
            window.ws.sendMessage(credentials);
        }
    });
});