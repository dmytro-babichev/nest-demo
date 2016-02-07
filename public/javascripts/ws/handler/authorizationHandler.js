define([], function () {
    return {
        handle: function(response) {
            if (response.status === 200 && response.sessionId !== undefined && response.sessionId !== null) {
                window.showSuccessMessage(response.message);
                localStorage.setItem("sessionId", response.sessionId);
                if (response.email !== undefined && response.email !== null) localStorage.setItem("email", response.email);
                window.router.navigate("main", {trigger: true, replace: true});
                return true;
            } else {
                window.showErrorMessage(response.message);
                localStorage.removeItem("sessionId");
                localStorage.removeItem("email");
                window.router.navigate("", {trigger: false, replace: true});
                window.router.login();
                return false;
            }
        }
    };
});