import config from "../../config";

let mockUsers = [];
let mockListen = undefined;
let socket = null;
let socketConnected = false;
let socketQueue = [];
let messageListener = undefined;
let usersListener = undefined;

const WebSocketApi = {
  open: afterOpen => {
    console.log("open websocket api");
    socket = new WebSocket(config.chatUrl);
    socket.onopen = function() {
      console.log("clientWebSocket.onopen", socket);
      afterOpen();
      if (socketQueue.length > 0) {
        let q = socketQueue;
        socketQueue = [];
        for (let i = 0; i < q.length; i++) {
          console.log("Send from queue", q[i]);
          socket.send(q[i]);
        }
      }
      socketConnected = true;
    };

    socket.onclose = function(data) {
      console.log("clientWebSocket.onclose", socket, data);
      socketConnected = false;
    };
    socket.onerror = function(error) {
      console.log("clientWebSocket.onerror", socket, error);
    };
    socket.onmessage = function(msg) {
      console.log("clientWebSocket.onmessage", socket, msg);
      try {
        let message = JSON.parse(msg.data);
        if (message) {
          if (message.type === "message") {
            if (messageListener !== undefined) {
              messageListener(message);
            }
          } else if (message.type === "users") {
            if (usersListener !== undefined) {
              usersListener(message.users);
            }
          } else if (message.type === "getUsers") {
            // Multicast ignore
          } else if (message.type === "newUser") {
            // Multicast ignore
          } else {
            console.warn("Unknown message type: ", message.type);
          }
        }
      } catch (e) {
        console.warn("Failed to process incoming message", e);
      }
    };
  },
  close: userName => {
    console.log("close", userName);
    socket.close();
  },
  newUser: userName => {
    mockUsers.push(userName);
    console.log("newUser", userName);
    socket.send(JSON.stringify({ type: "newUser", userName: userName }));
  },
  listenMessage: handler => (messageListener = handler),
  send: message => {
    console.log("send", message);
    if (socket !== null && socketConnected) {
      console.log("Sending", message);
      socket.send(JSON.stringify(Object.assign({}, Object.assign({ type: "message" }, message))));
    } else {
      console.log("Queueing", message);
      socketQueue.push(JSON.stringify(Object.assign({}, Object.assign({ type: "message" }, message))));
    }
  },
  getUsers: handler => {
    if (handler !== undefined) {
      usersListener = handler;
      if (socket != null && socketConnected) {
        socket.send(JSON.stringify({ type: "getUsers" }));
      } else {
        socketQueue.push(JSON.stringify({ type: "getUsers" }));
      }
    }
  }
};

const MockApi = {
  open: afterOpen => {
    console.log("open mock api");
    afterOpen();
  },
  close: userName => {
    console.log("close", userName);
  },
  newUser: userName => {
    mockUsers.push(userName);
    console.log("newUser", userName);
  },
  listenMessage: handler => (mockListen = handler),
  send: message => {
    console.log("send", message);
    if (mockListen !== undefined) mockListen(message);
  },
  getUsers: handler => {
    if (handler !== undefined) {
      console.log("getUsers", mockUsers);
      handler(mockUsers);
    }
  }
};

export const api =
  config.chatMockApi === undefined || config.chatMockApi === true
    ? MockApi
    : WebSocketApi;
