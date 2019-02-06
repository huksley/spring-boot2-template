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
      console.log("clientWebSocket.readyState", "websocketstatus");
      afterOpen();
      if (socketQueue.length > 0) {
        let q = socketQueue;
        socketQueue = [];
        for (let i = 0; i < q.length; i++) {
          socket.send(q[i]);
        }
      }
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
      let message = JSON.parse(msg.data);
      if (message) {
        if (message.type === "message") {
          if (messageListener !== undefined) {
            messageListener(message.message);
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
      socket.send(JSON.stringify({ type: "message", message: message }));
    } else {
      socketQueue.push(JSON.stringify({ type: "message", message: message }));
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
