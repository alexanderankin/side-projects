import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import 'bootstrap/dist/css/bootstrap.css'
import { QueryClient, QueryClientProvider } from "react-query";
import { createBrowserRouter, RouterProvider, } from "react-router-dom";
import { Citation } from "./Citation";
import { NewCitationPage } from "./CitationNew";
import { ErrorPage } from "./Reusable";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/citations/:citationId",
        element: <Citation />
      }
    ]
  },
  {
    path: "/new-citation",
    element: <NewCitationPage />,
    errorElement: <ErrorPage />
  }
]);

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
