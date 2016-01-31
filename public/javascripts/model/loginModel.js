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
            console.log("Sending credentials", this.get("email"), this.get("password"));
        }
    });
});