define([], function() {

    return {
        connect: function(url) {
            // Let us open a web socket
            var ws = new WebSocket(url);

            ws.onopen = function() {
                // Web Socket is connected
                console.log("Web socket is opened");
            };

            ws.onclose = function() {
                // Web Socket is connected
                console.log("Web socket is closed");
            };

            ws.onmessage = function(evt) {
                var receivedMsg = evt.data;
                console.log("Received message", receivedMsg);
            };

            return ws;
        }
    };
});