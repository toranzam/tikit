import { Link } from 'react-router'
import searchIcon from '../../assets/navigation/search.svg'
import heartIcon from '../../assets/navigation/heart.svg'
import myPageIcon from '../../assets/navigation/my-page.svg'
import shoppingBagIcon from '../../assets/navigation/shopping-bag.svg'
import { useAuthStore } from '../../stores/authStore'

const genres = ['연극', '무용', '대중무용', '클래식', '국악', '대중음악', '복합', '서커스/마술', '뮤지컬']

export default function NavBar() {
    const isLoggedIn = useAuthStore((state) => state.tokens !== null)

  return (
    <header className="w-full bg-white">
      <div className="mx-auto flex min-h-16 max-w-[1320px] flex-wrap items-center justify-between gap-y-4 pl-5 pt-6 max-sm:pr-5">
        <Link to="/" aria-label="TIKIT 홈" className="flex h-10 items-center text-[32px] leading-[normal] font-semibold">TIKIT</Link>
        <div className="flex h-10 w-[400px] max-w-full items-center justify-between gap-4 max-sm:w-full">
          <div role="search" className="flex h-10 w-[280px] min-w-0 items-center gap-3 rounded-full border border-[#edededd9] px-3 max-sm:flex-1">
            <input type="search" aria-label="공연 검색" placeholder="최현우 30주년 기념 마술쇼" className="h-full min-w-0 flex-1 bg-transparent text-sm leading-[normal] placeholder:text-[#8e8e8e]" />
            <button type="button" aria-label="검색" className="flex shrink-0 items-center justify-center"><img src={searchIcon} alt="" /></button>
          </div>
          <div className="flex h-full shrink-0 items-center gap-3 px-1">
            <button type="button" aria-label="관심 공연" className="flex h-6 w-[18.236px] items-center justify-center"><img src={heartIcon} alt="" className="max-w-none shrink-0" /></button>
              {/* 로그인 여부에 따라 로그인 <--> 마이페이지 경로 변경*/}
              <Link
                  to={isLoggedIn ? '/mypage' : '/login'}
                  aria-label={isLoggedIn ? '마이페이지' : '로그인'}
                  className="flex items-center justify-center"
              >
                  <img src={myPageIcon} alt="" />
              </Link>
            <button type="button" aria-label="예매 내역" className="flex items-center justify-center"><img src={shoppingBagIcon} alt="" /></button>
          </div>
        </div>
      </div>
      <nav aria-label="공연 장르" className="mx-auto flex h-12 max-w-[1320px] gap-[10px] overflow-x-auto px-5">
        {genres.map((genre) => (
          <button key={genre} type="button" className="shrink-0 px-3 text-lg leading-[normal] font-medium whitespace-nowrap first:pl-1">{genre}</button>
        ))}
      </nav>
    </header>
  )
}
