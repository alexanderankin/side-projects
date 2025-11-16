import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";

import "./index.css";
import "bootstrap/dist/css/bootstrap.css";
import { App, Create, Edit, Launch, Play, PlayList, Settings, Welcome } from "./App.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, element: <Welcome /> },
      { path: "settings", element: <Settings /> },
      { path: "create", element: <Create /> },
      { path: "create/:id", element: <Edit /> },
      { path: "launch/:id", element: <Launch /> },
      { path: "play", element: <PlayList /> },
      { path: "play/:id", element: <Play /> },
    ],
  },
]);

const queryClient = new QueryClient();
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
);
