import React, { useEffect, useState } from "react";
import logo from "./logo.svg";
import "./App.css";
import {
  MainContainer,
  ChatContainer,
  MessageList,
  Message,
  MessageInput,
  TypingIndicator,
} from "@chatscope/chat-ui-kit-react";
import "@chatscope/chat-ui-kit-styles/dist/default/styles.min.css";
import { v4 as uuidv4 } from "uuid";
import ReactMarkdown from "react-markdown";

// const API_KEY = "";

// const systemMessage = {
//   role: "system",
//   content:
//     "Explain things like you're talking to a software professional with 2 years of experience.",
// };
function App() {
  const storedUniqueId = localStorage.getItem("uuid");
  const id = storedUniqueId || uuidv4();
  localStorage.setItem("uuid", id);

  useEffect(() => {
    window.addEventListener("beforeunload", () => {
      localStorage.removeItem("uuid");
    });
  }, []);
  const [messages, setMessages] = useState([
    {
      message: "Hello, I'm llama2! Ask me anything!",
      sentTime: "just now",
      sender: "llama2",
    },
  ]);
  const [isTyping, setIsTyping] = useState(false);

  const handleSend = async (message: any) => {
    const newMessage = {
      message,
      direction: "outgoing",
      sender: "user",
    };
    const newMessages: any = [...messages, newMessage];
    console.log("newMessages: ",newMessages)
    setMessages(newMessages);
    setIsTyping(true);
    await processMessageToChatGPT(newMessages);
  };

  function stripHtml(html: string): string {
    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = html;
    return tempDiv.textContent || tempDiv.innerText || "";
  }

  
  async function processMessageToChatGPT(chatMessages: any) {
    // messages is an array of messages
    // Format messages for chatGPT API
    // API is expecting objects in format of { role: "user" or "assistant", "content": "message here"}
    // So we need to reformat
    console.log(
      "chatMessages: ",
      stripHtml(chatMessages[chatMessages.length - 1].message)
    );
    let apiMessages = chatMessages.map((messageObject: any) => {
      let role = "";
      if (messageObject.sender === "llama2") {
        role = "assistant";
      } else {
        role = "user";
      }
      return { role: role, content: messageObject.message };
    });

    // Get the request body set up with the model we plan to use
    // and the messages which we formatted above. We add a system message in the front to'
    // determine how we want chatGPT to act.
    // const apiRequestBody = {
    //   model: "gpt-3.5-turbo",
    //   messages: [
    //     systemMessage, // The system message DEFINES the logic of our chatGPT
    //     ...apiMessages, // The messages from our chat with ChatGPT
    //   ],
    // };
    try {
      const response = await fetch(`http://localhost:8080/ai/query`, {
     method: "POST",
     headers: {
       "Content-Type": "application/json",
      //  "Access-Control-Allow-Origin": "*",
      //  "Access-Control-Allow-Methods": "GET, POST, OPTIONS, PUT, DELETE",
      //  "Access-Control-Allow-Headers":
      //    "Origin, X-Requested-With, Content-Type, Accept, Authorization",
     },
     body: JSON.stringify({
       query: stripHtml(chatMessages[chatMessages.length - 1].message),
     }),
   });

   if (!response.ok) {
     throw new Error('Network response was not ok');
   }
   const data = await response.text();
   setMessages([
     ...chatMessages,
     {
       message: data,
       sender: "llama2",
     },
   ]);
   setIsTyping(false);
    } catch (error) {
      console.error(
        "There has been a problem with your fetch operation: ",
        error
      );
    }
  }

  return (
    <div className="App" style={{ height: "100vh", width: "60vw"}}>
      <div style={{ height: "90vh", margin: "10px 0px 10px 50px", }}>
        <MainContainer>
          <ChatContainer>
            <MessageList
              scrollBehavior="auto"
              typingIndicator={
                isTyping ? <TypingIndicator content="llama2 is thinking" /> : null
              }
            >
              {messages.map((message, i) => {
                // return (
                //   <Message
                //     key={i}
                //     model={{
                //       direction: "incoming",
                //       position: "first",
                //       ...message,
                //     }}

                //   />
                // );
                return (
                  <Message
                    key={i}
                    model={{
                      direction: message.sender === "user" ? "outgoing" : "incoming",
                      position: "single",
                      ...message,
                    }}
                  >
                    <Message.CustomContent>
                      <ReactMarkdown>{message.message}</ReactMarkdown>
                    </Message.CustomContent>
                  </Message>
                );
              })}
            </MessageList>
            <MessageInput
              placeholder="Type message here"
              onSend={handleSend}
              attachDisabled={false}
            />
          </ChatContainer>
        </MainContainer>
      </div>
    </div>
  );
}

export default App;
