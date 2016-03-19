define(['backbone', 'jquery', 'underscore', 'text!../../template/main.html', "ws/handler/nestOperationHandler"],
    function(Backbone, $, _, mainTemplate, NestOperationHandler) {

        return Backbone.View.extend({
            template: _.template(mainTemplate),

            events: {
            },

            initialize: function() {
                window.ws.addHandler(NestOperationHandler(this));
            },

            render: function() {
                this.$el.html(this.template(this.model));
                return this;
            },

            activate: function() {
                if (!isDefined(nestCode) || sessionValue(nestCode) === "") {
                    window.ws.sendMessage({action: "generate_nest_link", email: localStorage.getItem("email"), actionType: "nest_operation"});
                } else {
                    window.ws.sendMessage({action: "generate_access_token", email: localStorage.getItem("email"),
                        code: window.nestCode, actionType: "nest_operation"});
                }
            },

            generateAccessTokenMode: function(nestLink) {
                this.$el.find("a.nest-link").attr("href", nestLink);
                this.showContainer(".nest-link-container");
            },

            webCamMode: function() {
                this.showContainer(".web-cam-container");
            },

            showContainer: function(container) {
                this.$el.find(".row").hide();
                this.$el.find(container).show();
            }
        });
    }
);