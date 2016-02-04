define([], function () {
    return {
        handle: function(response) {
            if (response.status === 200 && response.sessionId !== undefined && response.sessionId !== null) {
                localStorage.setItem("sessionId", response.sessionId);
                localStorage.setItem("email", response.email);
                window.router.navigate("main", {trigger: true, replace: true});
                return true;
            } else {
                localStorage.removeItem("sessionId");
                localStorage.removeItem("email");
                window.router.navigate("", {trigger: false, replace: true});
                window.router.login();
                return false;
            }
        }
    };
});