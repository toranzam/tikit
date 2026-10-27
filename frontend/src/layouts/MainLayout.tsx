import { Outlet } from 'react-router'
import NavBar from '../components/layout/NavBar'

export default function MainLayout() {
  return (
    <div className="flex min-h-svh flex-col">
      <NavBar />
      <main className="flex-1"><Outlet /></main>
    </div>
  )
}
