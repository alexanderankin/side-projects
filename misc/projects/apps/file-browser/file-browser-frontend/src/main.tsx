import React from 'react'
import ReactDOM from 'react-dom/client'
import {
  createBrowserRouter,
  RouterProvider,
} from 'react-router-dom';
import { QueryClient, QueryClientProvider, useQuery } from 'react-query'

import App from './App.tsx'
import './index.css'
import fakeServer from './mirage-server.tsx';

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
  },
]);

fakeServer();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={new QueryClient()}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </React.StrictMode>,
)
