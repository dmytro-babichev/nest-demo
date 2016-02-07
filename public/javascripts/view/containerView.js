define(['backbone', 'jquery', 'text!../../template/container.html'], function(Backbone, $, containerTemplate) {
    return Backbone.View.extend({
            template: _.template(containerTemplate),

            events: {
            },

            render: function() {
                this.$el.html(this.template(this.model));
                return this;
            },

            loggedMode() {
                $(".navbar > .container-fluid").append('<ul class="nav navbar-nav navbar-right"><li><a>' + localStorage.getItem("email") + '</a></li></div>');
            }
        });
});