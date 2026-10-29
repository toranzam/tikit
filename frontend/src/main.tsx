import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import {RouterProvider} from "react-router/dom";
import root from "./router/root.tsx";
import AuthProvider from "./components/auth/AuthProvider";



createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={root}></RouterProvider>
    </AuthProvider>
  </StrictMode>,
)
