import { GetServerSideProps } from 'next'
import Cookies from 'js-cookie'
import { useRouter } from 'next/router'
import Link from "next/link";

export default function HomePage({ isLoggedIn }: { isLoggedIn: boolean }) {
  const router = useRouter()
  const logout = () => {
    // remove auth cookies
    Cookies.remove('Kiri-Auth-Token', { path: '/kiri' })
    Cookies.remove('Kiri-User-Id', { path: '/kiri' })
    router.reload()
  }

  return (
    <div>
      <Link href="/login">Login</Link>
      {/*{isLoggedIn ? (*/}
      {/*  <>*/}
      {/*    <p>You are logged in</p>*/}
      {/*    <button onClick={logout}>Logout</button>*/}
      {/*  </>*/}
      {/*) : (*/}
      {/*  <a href="/kiri/login">Login with Telegram</a>*/}
      {/*)}*/}
    </div>
  )
}

export const getServerSideProps: GetServerSideProps = async ({ req }) => {
  const cookies = req.headers.cookie || ''
  const isLoggedIn = cookies
    .split(';')
    .some(c => c.trim().startsWith('Kiri-Auth-Token='))
  return { props: { isLoggedIn } }
}
