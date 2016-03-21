define([], function () {

    return {
        handle: function(response) {
            if (response.status === 200 && isDefined(response.sessionId)) {
                window.showSuccessMessage(response.message);
                localStorage.setItem("sessionId", response.sessionId);
                if (isDefined(response.email)) localStorage.setItem("email", response.email);
                window.router.navigate("main", {trigger: true, replace: true});
                console.log("Action is authorized")
                return true;
            } else {
                console.log("Authorization error")
                window.showErrorMessage(response.message);
                window.router.basicView.logout();
                return false;
            }
        },
        name: "authorizationHandler"
    };
});