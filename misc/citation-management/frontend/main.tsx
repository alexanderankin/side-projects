import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import 'bootstrap/dist/css/bootstrap.css'
import { QueryClient, QueryClientProvider } from "react-query";
import { createBrowserRouter, RouterProvider, } from "react-router-dom";
import { Citation } from "./Citation";
import { NewCitation } from "./CitationNew";
import { ErrorPage, Header } from "./reusable";
import { Citations } from "./Citations";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    errorElement: <ErrorPage />,
  },
  {
    path: "/citations/:citationId",
    element: <><Header/><Citation /></>,
    errorElement: <ErrorPage />,
  },
  {
    path: "/citations",
    element: <><Header/><Citations /></>,
    errorElement: <ErrorPage />,
  },
  {
    path: "/new-citation",
    element: <><Header /><NewCitation /></>,
    errorElement: <ErrorPage />
  }
], {
  basename: '/thing'
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={new QueryClient({
      defaultOptions: {
        queries: {
          retry: false
        }
      }
    })}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
