define([], function () {

    function getUrlVar(name) {
        var vars = [], hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for(var i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            vars.push(hash[0]);
            vars[hash[0]] = hash[1];
        }
        return vars[name].split("#")[0];
    }

    return {
        handle: function(response) {
            if (response.status === 200 && response.sessionId !== undefined && response.sessionId !== null) {
                window.showSuccessMessage(response.message);
                localStorage.setItem("sessionId", response.sessionId);
                if (response.email !== undefined && response.email !== null) localStorage.setItem("email", response.email);
                var nestCodeValue = getUrlVar("code");
                if (nestCodeValue !== undefined && nestCodeValue !== null) localStorage.setItem("code", nestCodeValue);
                window.router.navigate("main", {trigger: true, replace: true});
                return true;
            } else {
                window.showErrorMessage(response.message);
                localStorage.removeItem("sessionId");
                localStorage.removeItem("email");
                localStorage.removeItem("code");
                window.router.navigate("", {trigger: false, replace: true});
                window.router.login();
                return false;
            }
        }
    };
});