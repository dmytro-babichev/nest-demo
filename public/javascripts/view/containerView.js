define(['backbone', 'jquery', 'underscore', 'text!../../template/container.html', 'text!../../template/navbarLoggedMode.html'],
    function(Backbone, $, _, containerTemplate, navbarLoggedMode) {
        return Backbone.View.extend({
                template: _.template(containerTemplate),

                events: {
                    "click #logoutBtn" : "logout"
                },

                render: function() {
                    this.$el.html(this.template(this.model));
                    return this;
                },

                loggedMode: function() {
                    $(".navbar > .container-fluid").html(_.template(navbarLoggedMode)({email: localStorage.getItem("email")}));
                },

                notLoggedMode: function() {
                    $(".navbar > .container-fluid > .navbar-collapse").remove();
                },

                logout: function() {
                    localStorage.clear();
                    window.location = window.location.pathname
                }
            });
    }
);