define(['backbone', 'jquery', 'underscore', 'text!../../template/main.html'],
    function(Backbone, $, _, mainTemplate) {

        return Backbone.View.extend({
            template: _.template(mainTemplate),

            events: {
            },

            render: function() {
                this.$el.html(this.template(this.model));
                return this;
            },

            init: function() {
                var self = this;
                if (localStorage.getItem("code") === undefined || localStorage.getItem("code") === null) {
                    window.ws.addHandler({handle: function(response) {
                        console.log("gete")
                        self.$el.find("a.nest-link").attr("href", response["nest_link"]);
                    }});
                    window.ws.sendMessage({action: "generate_nest_link", email: localStorage.getItem("email")});
                }
            }
        });
    }
);