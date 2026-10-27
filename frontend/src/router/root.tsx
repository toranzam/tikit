import { createBrowserRouter } from 'react-router'
import MainLayout from '../layouts/MainLayout'
import LoginPage from '../pages/auth/LoginPage'

const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: '/', element: <div>Hello World</div> },
      { path: '/login', element: <LoginPage /> },
    ],
  },
])

export default router
