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
        }
    });
});