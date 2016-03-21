define([], function () {

    return function(view) {
        return {
            handle: function(response) {
                if (response.action === "generate_nest_link") {
                    view.generateAccessTokenMode(response["nest_link"]);
                } else if (response.action === "generate_access_token") {
                    view.webCamMode(response);
                }
                return true;
            },
            name: "nestOperationHandler"
        };
    };
});