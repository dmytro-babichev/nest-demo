define(['backbone', 'underscore'], function(Backbone, _) {

    return Backbone.Model.extend({
        defaults: {
            email: "",
            password: "",
            sessionId: ""
        },
        login: function() {
            var credentials = _.extend(this.toJSON(), {"action": "login"})
            window.ws.sendMessage(credentials);
        },
        register: function() {
            var credentials = _.extend(this.toJSON(), {"action": "register"})
            window.ws.sendMessage(credentials);
        }
    });
});