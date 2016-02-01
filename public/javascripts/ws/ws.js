define(['underscore'], function(_) {

    return {
        connect: function(url) {
            // Let us open a web socket
            var ws = new WebSocket(url);
            var handlers = [];

            ws.onopen = function() {
                // Web Socket is connected
                console.log("Web socket is opened");
            };

            ws.onclose = function() {
                // Web Socket is connected
                console.log("Web socket is closed");
            };

            ws.onmessage = function(evt) {
                var receivedMsg = JSON.parse(evt.data);
                _.find(handlers, function(handler) {
                    return handler.handle(receivedMsg);
                });
            };

            ws.sendMessage = function(message) {
                var richMsg = _.extend(message, {"sessionId": localStorage.getItem("sessionId")});
                this.send(JSON.stringify(richMsg));
            }

            ws.addHandler = function(handler) {
                handlers.push(handler);
            }

            return ws;
        }
    };
});