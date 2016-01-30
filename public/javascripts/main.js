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
    }
});

requirejs(['jquery', 'backbone', 'underscore', 'bootstrap'], function($, Backbone, _) {
    //jQuery, canvas and the app/sub module are all
    //loaded and can be used here now.
    console.log($("body").html())
});
