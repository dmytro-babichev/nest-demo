define(['underscore'], function(_) {

    return {
        connect: function(url) {
            // Let us open a web socket
            var ws = new WebSocket(url);
            var handlers = [];
            var isOpen = false;
            var delayedMessages = [];

            ws.onopen = function() {
                // Web Socket is connected
                console.log("Web socket is opened");
                isOpen = true;
                _.each(delayedMessages, function(message) {
                    this.send(message);
                }, this);
                delayedMessages = [];
            };

            ws.onclose = function() {
                // Web Socket is closed
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
                var richMsgStr = JSON.stringify(richMsg);
                if (isOpen === true) {
                    this.send(richMsgStr);
                } else {
                    delayedMessages.push(richMsgStr); // web socket is still in CONNECTING state, but we already send messages via it
                }
            }

            ws.addHandler = function(handler) {
                handlers.push(handler);
            }

            return ws;
        }
    };
});