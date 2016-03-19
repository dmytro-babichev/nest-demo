define([], function () {

    return {
        handle: function(response) {
            if (response.status === 200 && isDefined(response.sessionId)) {
                window.showSuccessMessage(response.message);
                localStorage.setItem("sessionId", response.sessionId);
                if (isDefined(response.email)) localStorage.setItem("email", response.email);
                window.router.navigate("main", {trigger: true, replace: true});
                return true;
            } else {
                window.showErrorMessage(response.message);
                localStorage.clear();
                window.router.navigate("", {trigger: false, replace: true});
                window.router.login();
                return false;
            }
        },
        name: "authorizationHandler"
    };
});