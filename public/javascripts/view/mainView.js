define(['backbone', 'jquery', 'underscore', 'text!../../template/main.html'],
    function(Backbone, $, _, mainTemplate) {

        return Backbone.View.extend({
            template: _.template(mainTemplate),

            events: {
            },

            initialize: function() {
                var self = this;
                window.ws.addHandler({
                    handle: function(response) {
                        if (response.action === "generate_nest_link") {
                            self.$el.find("a.nest-link").attr("href", response["nest_link"]);
                        }
                        return true;
                    },
                    name: "generateLinkHandler"
                });
            },

            render: function() {
                this.$el.html(this.template(this.model));
                return this;
            },

            activate: function() {
                if (localStorage.getItem("code") === undefined || localStorage.getItem("code") === null) {
                    console.log("cx")
                    window.ws.sendMessage({action: "generate_nest_link", email: localStorage.getItem("email"), actionType: "nest_operation"});
                } else {
                    window.ws.sendMessage({action: "generate_access_token", email: localStorage.getItem("email"),
                        code: localStorage.getItem("code"), actionType: "nest_operation"});
                }
            }
        });
    }
);