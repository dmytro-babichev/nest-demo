requirejs.config({
    baseUrl: "assets/javascripts",
    paths: {
        // the left side is the module ID,
        // the right side is the path to
        // the jQuery file, relative to baseUrl.
        // Also, the path should NOT include
        // the '.js' file extension. This example
        // is using jQuery 1.9.0 located at
        // js/lib/jquery-1.9.0.js, relative to
        // the HTML page.
        jquery: 'jquery-2.2.0.min',
        backbone: 'backbone-min',
        underscore: 'underscore-min',
        bootstrap: 'bootstrap.min'
    },
    shim: {
        'bootstrap': {
            deps: ['jquery']
        }
    }
});

require(['jquery', 'backbone', 'router/router', 'ws/ws', 'ws/handler/authorizationHandler'], function($, Backbone, Router, WebSocket, AuthorizationHandler) {
    //jQuery, canvas and the app/sub module are all
    //loaded and can be used here now.
    window.ws = WebSocket.connect("ws://localhost:9000/connect");
    window.ws.addHandler(AuthorizationHandler);
    window.router = new Router();
    Backbone.history.start({hashChange: true});

    window.showErrorMessage = function(msg) {
        showMessage($("div.alert-danger"), msg);
    };

    window.showSuccessMessage = function(msg) {
        showMessage($("div.alert-success"), msg);
    };

    function showMessage(element, msg) {
        if (msg !== undefined && msg !== null) {
            element.show().find("span.msgText").text(msg);
            setTimeout(function() {
                element.fadeOut('fast');
            }, 5000);
        }
    }

});
