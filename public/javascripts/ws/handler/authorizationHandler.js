define([], function () {
    return {
        handle: function(response) {
            if (response.status === 200 && response.sessionId !== undefined && response.sessionId !== null) {
                localStorage.setItem("sessionId", response.sessionId);
                window.router.navigate("main", {trigger: true, replace: true});
                return true;
            } else {
                localStorage.removeItem("sessionId");
                window.router.navigate("", {trigger: true, replace: true});
                return false;
            }
        }
    };
});